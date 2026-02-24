package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.dto.request.PaymentRequestDto;
import com.eazybrew.vend.dto.response.PaymentReturnDataResponseDto;
import com.eazybrew.vend.dto.response.PaymentStatusResponseDto;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.*;
import com.eazybrew.vend.model.enums.*;
import com.eazybrew.vend.paystack.PayStack;
import com.eazybrew.vend.paystack.dto.request.InitializeTransactionRequest;
import com.eazybrew.vend.paystack.dto.response.InitializeTransactionResponse;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.DeviceRepository;
import com.eazybrew.vend.repository.PaymentRequestRepository;
import com.eazybrew.vend.repository.TransactionRepository;
import com.eazybrew.vend.service.DeviceService;
import com.eazybrew.vend.service.PaymentRequestService;
import com.eazybrew.vend.service.StaffService;
import com.eazybrew.vend.service.VoucherService;
import com.eazybrew.vend.util.GeneratorUtils;
import com.eazybrew.vend.nomba.service.NombaService;
import com.eazybrew.vend.websocket.WebSocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private final GeneratorUtils generatorUtils;
    private final WebSocketService webSocketService;
    private final PaymentRequestRepository paymentRequestRepository;
    private final DeviceRepository deviceRepository;
    private final CompanyRepository companyRepository;
    private final TransactionRepository transactionRepository;
    private final PayStack payStack;
    private final NombaService nombaService;

    @Override
    @Transactional
    public TransactionResponse processPaymentRequest(PaymentRequestDto request) {
        log.info("Processing payment request: {}", request);
        BigDecimal divisor = BigDecimal.valueOf(100);
        BigDecimal amountInCurrency = request.getAmount()
                .divide(divisor, 2, RoundingMode.HALF_EVEN);
        request.setAmount(amountInCurrency);

        Staff staff;
        if (transactionRepository.existsByReferenceNumber(request.getTransactionReference())) {
            throw new CustomException("Transaction reference number already exists", HttpStatus.BAD_REQUEST);
        }

        // Create and save the payment request
        PaymentType paymentType = PaymentType.fromValue(request.getPaymentType());
        if (paymentType == null) {
            throw new CustomException("Invalid payment type: " + request.getPaymentType(), HttpStatus.BAD_REQUEST);
        }

        // Validate API key and get company
        Company company = companyRepository
                .findByApiKeyAndRecordStatus(request.getApiKey(), RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Invalid API key", HttpStatus.UNAUTHORIZED));

        if (!company.isEnabled()) {
            throw new CustomException("Company account is disabled", HttpStatus.FORBIDDEN);
        }

        // Get device
        Device device = deviceRepository.findByVendingMachineIdAndRecordStatus(request.getVendingMachineId(),
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
        PaymentMethod paymentMethod;
        InitializeTransactionRequest initializeTransactionRequest;
        InitializeTransactionResponse initializeTransactionResponse;

        // initialize Transaction
        Transaction transaction = null;
        if (paymentType == PaymentType.NFC) {
            paymentMethod = PaymentMethod.NFC;
        } else if (paymentType == PaymentType.BANK_CARD) {
            // ---- Nomba Terminal Push Payment ----
            paymentMethod = PaymentMethod.CREDIT_CARD;

            String terminalId = device.getNombaTerminalId();
            if (terminalId == null || terminalId.isBlank()) {
                throw new CustomException("No Nomba terminal configured for this device", HttpStatus.BAD_REQUEST);
            }

            // Create the transaction as PENDING
            Transaction nombaTransaction = Transaction.builder()
                    .transactionId(generatorUtils.generateTransactionId())
                    .referenceNumber(request.getTransactionReference())
                    .transactionType(TransactionType.SALE)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .amount(amountInCurrency)
                    .status(TransactionStatus.PENDING)
                    .device(device)
                    .company(company)
                    .notes(request.getProductDescription())
                    .build();
            nombaTransaction = transactionRepository.saveAndFlush(nombaTransaction);

            // Update device last active
            device.setLastActive(nombaTransaction.getDateCreated());
            deviceRepository.save(device);

            // Push payment to Nomba terminal; returns Nomba's paymentId (used for webhook
            // lookup)
            String nombaPaymentId = nombaService.pushPaymentToTerminal(
                    terminalId, request.getTransactionReference(), amountInCurrency);

            // Store Nomba's paymentId in paystackReference — the webhook echoes this back
            // as merchantTxRef
            if (nombaPaymentId != null && !nombaPaymentId.isBlank()) {
                nombaTransaction.setPaystackReference(nombaPaymentId);
                nombaTransaction = transactionRepository.saveAndFlush(nombaTransaction);
                log.info("[Nomba] Stored nombaPaymentId={} on transaction ref={}", nombaPaymentId,
                        request.getTransactionReference());
            }

            log.info("Nomba terminal payment pushed for ref: {}", request.getTransactionReference());

            TransactionResponse nombaResponse = TransactionResponse.fromEntity(nombaTransaction);
            nombaResponse.setAuthorizationUrl("#");
            nombaResponse.setAccessCode("#");
            nombaResponse.setPaystackReference(request.getTransactionReference());
            nombaResponse.setEvent(paymentType.name());

            webSocketService.sendTransactionNotification(nombaResponse);

            PaymentRequest nombaPaymentRequest = PaymentRequest.builder()
                    .transactionReference(request.getTransactionReference())
                    .productName(request.getProductName())
                    .productDescription(request.getProductDescription())
                    .amount(amountInCurrency)
                    .paymentType(paymentType)
                    .status("PENDING")
                    .recordStatus(RecordStatusConstant.ACTIVE)
                    .build();
            paymentRequestRepository.save(nombaPaymentRequest);
            return nombaResponse;
            // ---- End Nomba Block ----
        } else if (paymentType == PaymentType.TRANSFER) {
            paymentMethod = PaymentMethod.PAYSTACK;
        } else {
            paymentMethod = PaymentMethod.PAYSTACK;
        }

        if (paymentMethod.name().equalsIgnoreCase(PaymentMethod.PAYSTACK.name())) {

            initializeTransactionRequest = InitializeTransactionRequest.builder()
                    .amount(amountInCurrency.multiply(BigDecimal.valueOf(100)).longValue())
                    .email("contact@bemoretec.com")
                    .callback_url(request.getCallbackUrl())
                    .build();

            initializeTransactionResponse = payStack
                    .initializeTransaction(initializeTransactionRequest);

            // Create transaction
            transaction = Transaction.builder()
                    .transactionId(generatorUtils.generateTransactionId())
                    .referenceNumber(request.getTransactionReference())
                    .transactionType(TransactionType.SALE)
                    .paymentMethod(paymentMethod)
                    .amount(amountInCurrency)
                    .status(TransactionStatus.PENDING) // Default to completed
                    .device(device)
                    .company(company)
                    .notes(request.getProductDescription())
                    .paystackReference(initializeTransactionResponse.getData().getReference())
                    .build();

            // Save transaction
            transaction = transactionRepository.saveAndFlush(transaction);
            // Update device last active timestamp
            device.setLastActive(transaction.getDateCreated());
            deviceRepository.save(device);

            log.info("Transaction processed successfully: {}", transaction.getTransactionId());

            TransactionResponse transactionResponse = TransactionResponse.fromEntity(transaction);
            transactionResponse.setAuthorizationUrl(initializeTransactionResponse.getData().getAuthorization_url());
            transactionResponse.setAccessCode(initializeTransactionResponse.getData().getAccess_code());
            transactionResponse.setPaystackReference(initializeTransactionResponse.getData().getReference());
            transactionResponse.setEvent(paymentType.name());

            // Send WebSocket notification about the transaction
            webSocketService.sendTransactionNotification(transactionResponse);
            log.info("WebSocket notification sent for transaction: {}", transaction.getTransactionId());

            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .transactionReference(request.getTransactionReference())
                    .productName(request.getProductName())
                    .productDescription(request.getProductDescription())
                    .amount(amountInCurrency)
                    .paymentType(paymentType)
                    .status("PENDING") // Initial status is PENDING
                    .recordStatus(RecordStatusConstant.ACTIVE)
                    .build();

            paymentRequestRepository.save(paymentRequest);
            return transactionResponse;
        }

        String transactionRef = request.getTransactionReference();

        // Create transaction
        transaction = Transaction.builder()
                .transactionId(transactionRef)
                .referenceNumber(transactionRef)
                .transactionType(TransactionType.SALE)
                .paymentMethod(paymentMethod)
                .amount(amountInCurrency)
                .status(TransactionStatus.PENDING)
                .device(device)
                .company(company)
                .paystackReference(transactionRef)
                .notes(request.getProductDescription())
                .build();

        // Save transaction
        transaction = transactionRepository.saveAndFlush(transaction);
        // Update device last active timestamp
        device.setLastActive(transaction.getDateCreated());
        deviceRepository.save(device);

        log.info("Transaction processed successfully: {}", transaction.getTransactionId());

        TransactionResponse transactionResponse = TransactionResponse.fromEntity(transaction);
        transactionResponse.setAuthorizationUrl("#");
        transactionResponse.setAccessCode("#");
        transactionResponse.setPaystackReference(transactionRef);
        transactionResponse.setEvent(paymentType.name());

        // Send WebSocket notification about the transaction
        webSocketService.sendTransactionNotification(transactionResponse);
        log.info("WebSocket notification sent for transaction: {}", transaction.getTransactionId());

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .transactionReference(request.getTransactionReference())
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .amount(request.getAmount())
                .paymentType(paymentType)
                .status("PENDING") // Initial status is PENDING
                .recordStatus(RecordStatusConstant.ACTIVE)
                .build();

        paymentRequestRepository.save(paymentRequest);
        return transactionResponse;
    }

    @Async
    public CompletableFuture<Void> simulatePaymentProcessing(String transactionReference) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate processing delay
                Thread.sleep(5000);

                // Update payment status to SUCCESS
                PaymentRequest paymentRequest = paymentRequestRepository
                        .findByTransactionReferenceAndRecordStatus(transactionReference, RecordStatusConstant.ACTIVE)
                        .orElse(null);

                if (paymentRequest != null) {
                    paymentRequest.setStatus("SUCCESS");
                    paymentRequest.setReturnUrl(
                            "https://dev.eazybrewserver.com/payment/receipt?reference=" + transactionReference);
                    paymentRequestRepository.save(paymentRequest);
                    log.info("Payment request completed successfully: {}", transactionReference);
                }
            } catch (InterruptedException e) {
                log.error("Error processing payment request: {}", e.getMessage());
            }
        });
    }

    @Override
    public TransactionResponse getPaymentStatus(String transactionReference) {
        log.info("Getting payment status for transaction reference: {}", transactionReference);

        Transaction transaction = transactionRepository
                .findByReferenceNumber(transactionReference)
                .orElseThrow(() -> new CustomException("Transaction with ref not found", HttpStatus.NOT_FOUND));

        TransactionResponse transactionResponse = TransactionResponse.fromEntity(transaction);

        return transactionResponse;
    }

    @Override
    public PaymentReturnDataResponseDto getPaymentReturnData(String transactionReference) {
        log.info("Getting payment return data for transaction reference: {}", transactionReference);

        PaymentRequest paymentRequest = paymentRequestRepository
                .findByTransactionReferenceAndRecordStatus(transactionReference, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Payment request not found", HttpStatus.NOT_FOUND));

        // Check if the transaction is successful
        if (!"SUCCESS".equals(paymentRequest.getStatus())) {
            throw new CustomException("Payment not successful", HttpStatus.BAD_REQUEST);
        }

        // Get the return URL from the payment request
        String returnUrl = paymentRequest.getReturnUrl();
        if (returnUrl == null || returnUrl.isEmpty()) {
            returnUrl = "https://dev.eazybrewserver.com/payment/receipt?reference=" + transactionReference;
        }

        return PaymentReturnDataResponseDto.builder()
                .status("success")
                .message("Payment return data retrieved successfully")
                .data(returnUrl)
                .build();
    }
}
