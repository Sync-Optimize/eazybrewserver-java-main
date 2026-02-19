package com.eazybrew.vend.websocket;

import com.eazybrew.vend.dto.response.TransactionResponse;
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

/**
 * Model class for WebSocket messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    private String type;
    private Long id;
    private String transactionId;
    private String referenceNumber;
    private TransactionType transactionType;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private TransactionStatus status;
    private String deviceSerialNumber;
    private String deviceName;
    private Long companyId;
    private String companyName;
    private String vendingMachineSerialNumber;
    private String vendingMachineName;
    private String notes;
    private LocalDateTime transactionDate;
    private String authorizationUrl;
    private String accessCode;
    private String paystackReference;
    private String event;

    /**
     * Create a WebSocketMessage from a Transaction
     * 
     * @param transaction the transaction
     * @param type the message type
     * @return a WebSocketMessage
     */
    public static WebSocketMessage fromTransaction(TransactionResponse transaction, String type) {
        WebSocketMessageBuilder builder = WebSocketMessage.builder()
                .type(type)
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .referenceNumber(transaction.getReferenceNumber())
                .paystackReference(transaction.getPaystackReference())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .transactionType(transaction.getTransactionType())
                .paymentMethod(transaction.getPaymentMethod())
                .companyId(transaction.getCompanyId())
                .companyName(transaction.getCompanyName())
                .deviceSerialNumber(transaction.getDeviceId())
                .deviceName(transaction.getDeviceName())
                .vendingMachineName(transaction.getVendingMachineName())
                .vendingMachineSerialNumber(transaction.getVendingMachineSerialNumber())
                .notes(transaction.getNotes())
                .event(transaction.getEvent())
                .accessCode(transaction.getAccessCode())
                .paystackReference(transaction.getPaystackReference())
                .authorizationUrl(transaction.getAuthorizationUrl());


//        // Add staff information if available
//        if (transaction.getStaff() != null) {
//            builder.staffId(transaction.getStaff().getId())
//                  .staffName(transaction.getStaff().getFullName());
//        }

        return builder.build();
    }
}
