package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.dto.request.TransactionItemRequest;
import com.eazybrew.vend.dto.request.TransactionRequest;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.*;
import com.eazybrew.vend.model.enums.PaymentMethod;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.model.enums.TransactionType;
import com.eazybrew.vend.paystack.PayStack;
import com.eazybrew.vend.paystack.dto.request.InitializeTransactionRequest;
import com.eazybrew.vend.paystack.dto.response.InitializeTransactionResponse;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.DeviceRepository;
import com.eazybrew.vend.repository.TransactionRepository;
import com.eazybrew.vend.service.DeviceService;
import com.eazybrew.vend.service.StaffService;
import com.eazybrew.vend.service.TransactionService;
import com.eazybrew.vend.service.VoucherService;
import com.eazybrew.vend.util.GeneratorUtils;
import com.eazybrew.vend.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    private final CompanyRepository companyRepository;
    private final TransactionRepository transactionRepository;
    private final StaffService staffService;
    private final VoucherService voucherService;
    private final PayStack payStack;
    private final GeneratorUtils generatorUtils;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        BigDecimal divisor = BigDecimal.valueOf(100);
        BigDecimal amountInCurrency = request.getAmount()
                .divide(divisor, 2, RoundingMode.HALF_EVEN);
        request.setAmount(amountInCurrency);
        // Check if device is registered and enabled
        if (!deviceService.isDeviceRegisteredAndEnabled(request.getDeviceId())) {
            throw new CustomException("Device is not registered or is disabled", HttpStatus.FORBIDDEN);
        }

        // Validate API key and get company
        Company company = companyRepository
                .findByApiKeyAndRecordStatus(request.getApiKey(), RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Invalid API key", HttpStatus.UNAUTHORIZED));

        if (!company.isEnabled()) {
            throw new CustomException("Company account is disabled", HttpStatus.FORBIDDEN);
        }

        Staff staff = null;
        if (PaymentMethod.NFC.name().equalsIgnoreCase(request.getPaymentMethod().toString())) {
            staff = staffService.getStaffByCardSerialNumberInternal(company.getId(), request.getCardSerial());

            if (!staff.isEnabled()) {
                throw new CustomException("Staff account is disabled", HttpStatus.FORBIDDEN);
            }
        }

        // Get device
        Device device = deviceRepository.findByDeviceIdAndRecordStatus(request.getDeviceId(),
                RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

        if (!device.isEnabled()) {
            throw new CustomException("Device is disabled", HttpStatus.FORBIDDEN);
        }

        // Validate that device belongs to the company
        if (!device.getCompany().getId().equals(company.getId())) {
            throw new CustomException("Device does not belong to this company", HttpStatus.FORBIDDEN);
        }

        // Validate amount
        if (amountInCurrency.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Transaction amount must be positive", HttpStatus.BAD_REQUEST);
        }
        // Check if staff or company has a valid voucher and can spend the requested
        // amount
        boolean useVoucher = false;
        boolean useStaffVoucher = false;

        if (PaymentMethod.NFC.name().equalsIgnoreCase(request.getPaymentMethod().toString())) {
            staff = staffService.getStaffByCardSerialNumberInternal(company.getId(), request.getCardSerial());
            log.info("using nfc and staff: {}", staff.getCardSerial());
            if (!staff.isEnabled()) {
                throw new CustomException("Staff account is disabled", HttpStatus.FORBIDDEN);
            }

            try {
                if (staff != null) {
                    // Check if staff has a voucher
                    useStaffVoucher = voucherService.canStaffSpend(company.getId(), staff.getId(), amountInCurrency);
                    log.info("Staff {} has a valid voucher and can spend {}: {}",
                            staff.getId(), amountInCurrency, useStaffVoucher);
                    useVoucher = useStaffVoucher;

                }

                // // If staff doesn't have a voucher or can't spend, check company voucher
                // if (!useVoucher) {
                // useVoucher = voucherService.canCompanySpend(company.getId(),
                // request.getAmount());
                // log.info("Company {} has a valid voucher and can spend {}: {}",
                // company.getId(), request.getAmount(), useVoucher);
                // }
            } catch (Exception e) {
                // If there's any error checking the voucher, log it and continue without using
                // voucher
                log.error("Error checking voucher: {}", e.getMessage());
                useVoucher = false;
                useStaffVoucher = false;
            }
        }

        if (!useVoucher && PaymentMethod.NFC.name().equalsIgnoreCase(request.getPaymentMethod().toString())) {
            throw new CustomException("Voucher has already been used or exhausted, try another payment method",
                    HttpStatus.BAD_REQUEST);
        }

        InitializeTransactionRequest initializeTransactionRequest;
        InitializeTransactionResponse initializeTransactionResponse;

        // If using voucher, we don't need to initialize a payment transaction
        if (useVoucher) {
            // Create a dummy response for voucher payment
            initializeTransactionResponse = new InitializeTransactionResponse();
            initializeTransactionResponse.setStatus(true);
            initializeTransactionResponse.setMessage("Voucher payment");

            InitializeTransactionResponse.Data data = new InitializeTransactionResponse.Data();
            data.setReference("VOUCHER-" + generatorUtils.generateTransactionId());
            data.setAccess_code("VOUCHER");
            data.setAuthorization_url("#");
            initializeTransactionResponse.setData(data);
            log.info("using nfc and staff dummy data: {}", staff.getCardSerial());
        } else {
            // Regular payment flow
            initializeTransactionRequest = InitializeTransactionRequest.builder()
                    .amount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .email("contact@bemoretec.com")
                    .build();
            if (staff != null) {
                initializeTransactionRequest.setEmail(staff.getEmail());
            }

            initializeTransactionResponse = payStack
                    .initializeTransaction(initializeTransactionRequest);
        }

        // Create transaction
        Transaction transaction;
        Optional<Transaction> transactionOptional = transactionRepository
                .findByReferenceNumber(request.getReferenceNumber());
        if (transactionOptional.isEmpty()
                && PaymentMethod.NFC.name().equalsIgnoreCase(request.getPaymentMethod().toString())) {
            throw new CustomException("Pass the reference use to start the transaction on the vending machine",
                    HttpStatus.BAD_REQUEST);
        }
        if (transactionOptional.isPresent()) {
            transaction = transactionOptional.get();
            if (staff != null && PaymentMethod.NFC.name().equalsIgnoreCase(request.getPaymentMethod().toString())) {
                transaction.setStaff(staff);
            }
            // Save transaction
            transaction = transactionRepository.saveAndFlush(transaction);
            log.info("using nfc and staff got the transaction: {}", staff.getCardSerial());
        } else {
            transaction = Transaction.builder()
                    .transactionId(generatorUtils.generateTransactionId())
                    .referenceNumber(request.getReferenceNumber())
                    .transactionType(TransactionType.SALE)
                    .paymentMethod(request.getPaymentMethod())
                    .amount(amountInCurrency)
                    .status(TransactionStatus.PENDING) // Default to completed
                    .device(device)
                    .company(company)

                    .paystackReference(initializeTransactionResponse.getData().getReference())
                    //
                    .build();
        }

        // Update device last active timestamp
        device.setLastActive(transaction.getDateCreated());
        deviceRepository.save(device);

        // If using voucher, deduct the amount from the voucher
        if (useVoucher) {
            try {
                if (useStaffVoucher && staff != null) {
                    // Use staff voucher
                    voucherService.useStaffVoucher(company.getId(), staff.getId(), amountInCurrency);
                    log.info("Successfully used staff voucher for staff {} to pay {}",
                            staff.getId(), amountInCurrency);
                }
                transaction.setStatus(TransactionStatus.COMPLETED);
                // Save transaction
                transaction = transactionRepository.saveAndFlush(transaction);
                log.info("using nfc and staff completed transaction: {}", staff.getCardSerial());
                // else {
                // // Use company voucher
                // voucherService.useVoucher(company.getId(), request.getAmount());
                // log.info("Successfully used company voucher for company {} to pay {}",
                // company.getId(), request.getAmount());
                // }
            } catch (Exception e) {
                // If there's any error using the voucher, log it but don't fail the transaction
                // since we've already created the transaction record
                log.error("Error using voucher: {}", e.getMessage());
            }
        }

        log.info("Transaction processed successfully: {}", transaction.getTransactionId());

        TransactionResponse transactionResponse = TransactionResponse.fromEntity(transaction);
        transactionResponse.setAuthorizationUrl(initializeTransactionResponse.getData().getAuthorization_url());
        transactionResponse.setAccessCode(initializeTransactionResponse.getData().getAccess_code());
        transactionResponse.setPaystackReference(initializeTransactionResponse.getData().getReference());

        // // Send WebSocket notification about the transaction
        // webSocketService.sendTransactionNotification(transaction);
        // log.info("WebSocket notification sent for transaction: {}",
        // transaction.getTransactionId());

        return transactionResponse;
    }

    @Override
    public Page<Transaction> getTransactionsByCompany(Long companyId, Pageable pageable) {
        // Validate and get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Unable to find company", HttpStatus.NOT_FOUND));

        return transactionRepository.findByCompany(company, pageable);
    }

    @Override
    public Page<Transaction> getTransactionsByCompanyAndStaffId(Long companyId, Long staffId, Pageable pageable) {

        // Validate and get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Unable to find company", HttpStatus.NOT_FOUND));

        // Validate and get Staff
        Staff staff = staffService.getStaffById(companyId, staffId);
        return transactionRepository.findByCompanyAndStaff(company, staff, pageable);
    }

    @Override
    public Page<Transaction> getTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    private TransactionItem createTransactionItem(TransactionItemRequest request, Transaction transaction) {
        return TransactionItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                .description(request.getDescription())
                .category(request.getCategory())
                // .transaction(transaction)
                .build();
    }
}
