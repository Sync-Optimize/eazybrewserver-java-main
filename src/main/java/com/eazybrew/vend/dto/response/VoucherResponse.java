package com.eazybrew.vend.dto.response;

import com.eazybrew.vend.model.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long staffId;
    private String staffName;
    private BigDecimal dailyLimit;
    private BigDecimal totalAmount;
    private BigDecimal usedAmountToday;
    private BigDecimal remainingDailyAmount;
    private LocalDate lastResetDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean enabled;
    private LocalDateTime dateCreated;
    private LocalDateTime dateModified;

    /**
     * Convert a Voucher entity to a VoucherResponse DTO
     * 
     * @param voucher the voucher entity
     * @return the voucher response DTO
     */
    public static VoucherResponse fromEntity(Voucher voucher) {
        if (voucher == null) {
            return null;
        }

        // Reset used amount if it's a new day
        voucher.resetUsedAmountIfNewDay();

        VoucherResponse.VoucherResponseBuilder builder = VoucherResponse.builder()
                .id(voucher.getId())
                .companyId(voucher.getCompany().getId())
                .companyName(voucher.getCompany().getName());

        // Add staff information if available
        if (voucher.getStaff() != null) {
            builder.staffId(voucher.getStaff().getId())
                   .staffName(voucher.getStaff().getFullName());
        }

        return builder
                .dailyLimit(voucher.getDailyLimit())
                .totalAmount(voucher.getTotalAmount())
                .usedAmountToday(voucher.getUsedAmountToday())
                .remainingDailyAmount(voucher.getDailyLimit().subtract(voucher.getUsedAmountToday()))
                .lastResetDate(voucher.getLastResetDate())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .enabled(voucher.isEnabled())
                .dateCreated(voucher.getDateCreated())
                .dateModified(voucher.getDateModified())
                .build();
    }
}
