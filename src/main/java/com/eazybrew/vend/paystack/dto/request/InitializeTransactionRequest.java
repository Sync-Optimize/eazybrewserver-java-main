package com.eazybrew.vend.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializeTransactionRequest {
    private String email;
    private BigDecimal amount;
}