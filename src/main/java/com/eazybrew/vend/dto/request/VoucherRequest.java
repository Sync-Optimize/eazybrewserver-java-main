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
public class VoucherRequest {

    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0.01", message = "Daily limit must be positive")
    private BigDecimal dailyLimit;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * List of staff IDs to assign the voucher to.
     * If null or empty, the voucher will be assigned to the company only.
     */
    private List<Long> staffIds;
}
