package com.eazybrew.vend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherFundRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    // Optional fields for creating a new voucher if one doesn't exist
    private BigDecimal dailyLimit;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * List of staff IDs to fund vouchers for.
     * If null or empty, the voucher will be funded for the company only.
     */
    private List<Long> staffIds;
}
