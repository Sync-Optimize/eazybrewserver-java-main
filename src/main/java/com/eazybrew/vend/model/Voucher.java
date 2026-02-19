package com.eazybrew.vend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vouchers")
public class Voucher extends BaseEntity<Long> {

    @NotNull
    @Column(nullable = false)
    private BigDecimal dailyLimit;

    @NotNull
    @Column(nullable = false)
    private BigDecimal totalAmount;

    @NotNull
    @Column(nullable = false)
    private BigDecimal usedAmountToday;

    @Column(nullable = false)
    private LocalDate lastResetDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;



    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Checks if the voucher is valid for use.
     * A voucher is valid if:
     * 1. It is enabled
     * 2. Current date is between startDate and endDate (inclusive)
     * 3. It has remaining total amount
     *
     * @return true if the voucher is valid, false otherwise
     */
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return enabled &&
                !today.isBefore(startDate) &&
                !today.isAfter(endDate) &&
                totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if the requested amount can be spent today.
     * This checks both the daily limit and the total remaining amount.
     *
     * @param requestedAmount the amount to check
     * @return true if the amount can be spent today, false otherwise
     */
    public boolean canSpendToday(BigDecimal requestedAmount) {
        // Reset used amount if it's a new day
        resetUsedAmountIfNewDay();

        // Check if requested amount is within daily limit
        BigDecimal remainingDailyLimit = dailyLimit.subtract(usedAmountToday);

        // Check both daily limit and total amount
        return requestedAmount.compareTo(remainingDailyLimit) <= 0 &&
               requestedAmount.compareTo(totalAmount) <= 0;
    }

    /**
     * Resets the used amount today if it's a new day.
     */
    public void resetUsedAmountIfNewDay() {
        LocalDate today = LocalDate.now();
        if (lastResetDate == null || !today.isEqual(lastResetDate)) {
            usedAmountToday = BigDecimal.ZERO;
            lastResetDate = today;
        }
    }

    /**
     * Spends the requested amount from the voucher.
     * This updates both the total amount and the used amount today.
     *
     * @param amount the amount to spend
     * @throws IllegalArgumentException if the amount cannot be spent
     */
    public void spend(BigDecimal amount) {
        if (!canSpendToday(amount)) {
            throw new IllegalArgumentException("Cannot spend requested amount");
        }

        totalAmount = totalAmount.subtract(amount);
        usedAmountToday = usedAmountToday.add(amount);
    }
}
