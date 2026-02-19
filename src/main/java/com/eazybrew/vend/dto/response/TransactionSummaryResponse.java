package com.eazybrew.vend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionSummaryResponse {
    private long companyCount;
    private long deviceCount;
    private long staffCount;
    private BigDecimal totalTransactionAmount;
}