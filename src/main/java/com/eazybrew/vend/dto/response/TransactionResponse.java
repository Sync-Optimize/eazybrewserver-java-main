package com.eazybrew.vend.dto.response;

import com.eazybrew.vend.model.Transaction;
import com.eazybrew.vend.model.enums.PaymentMethod;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionId;
    private String referenceNumber;
    private TransactionType transactionType;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private TransactionStatus status;
    private String deviceId;
    private String deviceName;
    private Long companyId;
    private String companyName;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String vendingMachineName;
    private String vendingMachineSerialNumber;
    private String notes;
    private LocalDateTime transactionDate;
    private String authorizationUrl;
    private String accessCode;
    private String paystackReference;
    private String event;
//    private List<TransactionItemResponse> items;

    public static TransactionResponse fromEntity(Transaction transaction) {
        TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .referenceNumber(transaction.getReferenceNumber())
                .transactionType(transaction.getTransactionType())
                .paymentMethod(transaction.getPaymentMethod())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .deviceId(transaction.getDevice().getDeviceId())
                .deviceName(transaction.getDevice().getDeviceName())
                .companyId(transaction.getCompany().getId())
                .companyName(transaction.getCompany().getName())
                .vendingMachineName(transaction.getDevice().getVendingMachineName() != null ?
                        transaction.getDevice().getVendingMachineName() : "")
                .vendingMachineSerialNumber(transaction.getDevice().getVendingMachineId() != null ?
                        transaction.getDevice().getVendingMachineId() : "")
//                .customerName(transaction.getStaff().getFullName())
//                .customerEmail(transaction.getStaff().getEmail())
//                .customerPhone(transaction.getCustomerPhone())
                .notes(transaction.getNotes())
                .transactionDate(transaction.getDateCreated());
        if(transaction.getStaff() != null) {
            builder.customerName(transaction.getStaff().getFullName())
                .customerEmail(transaction.getStaff().getEmail());
        }

//        // Add items if present
//        if (transaction.getItems() != null && !transaction.getItems().isEmpty()) {
//            List<TransactionItemResponse> itemResponses = transaction.getItems().stream()
//                    .map(TransactionItemResponse::fromEntity)
//                    .collect(Collectors.toList());
//            builder.items(itemResponses);
//        }

        return builder.build();
    }
}
