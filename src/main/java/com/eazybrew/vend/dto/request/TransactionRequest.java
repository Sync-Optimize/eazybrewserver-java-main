package com.eazybrew.vend.dto.request;

import com.eazybrew.vend.model.enums.PaymentMethod;
import com.eazybrew.vend.model.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Reference number is required")
    private String referenceNumber;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String cardSerial;

    private String customerEmail;

    private String customerPhone;

    @NotBlank(message = "API key is required")
    private String apiKey;

    private String notes;

    private String callbackUrl;

}