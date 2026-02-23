package com.eazybrew.vend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotBlank(message = "Vending Machine Serial Number Is Required")
    private String vendingMachineId;

    private String productName;

    private String productDescription;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    private int paymentType;

    private String apiKey;

    private String callbackUrl;
}
