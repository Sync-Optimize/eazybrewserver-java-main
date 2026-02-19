package com.eazybrew.vend.service;

import com.eazybrew.vend.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VoucherService {

    /**
     * Create a new voucher for a company
     * 
     * @param companyId the company ID
     * @param dailyLimit the daily spending limit
     * @param totalAmount the total amount of the voucher
     * @param startDate the start date of the voucher validity
     * @param endDate the end date of the voucher validity
     * @return the created voucher
     */
    Voucher createVoucher(Long companyId, BigDecimal dailyLimit, 
                          BigDecimal totalAmount, LocalDate startDate, LocalDate endDate);

    /**
     * Create a new voucher for a staff member
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @param dailyLimit the daily spending limit
     * @param totalAmount the total amount of the voucher
     * @param startDate the start date of the voucher validity
     * @param endDate the end date of the voucher validity
     * @return the created voucher
     */
    Voucher createStaffVoucher(Long companyId, Long staffId, BigDecimal dailyLimit, 
                              BigDecimal totalAmount, LocalDate startDate, LocalDate endDate);

    /**
     * Create vouchers for multiple staff members
     * 
     * @param companyId the company ID
     * @param staffIds the list of staff IDs
     * @param dailyLimit the daily spending limit
     * @param totalAmount the total amount of the voucher
     * @param startDate the start date of the voucher validity
     * @param endDate the end date of the voucher validity
     * @return the list of created vouchers
     */
    List<Voucher> createStaffVouchers(Long companyId, List<Long> staffIds, BigDecimal dailyLimit, 
                                     BigDecimal totalAmount, LocalDate startDate, LocalDate endDate);

    /**
     * Fund an existing voucher or create a new one if it doesn't exist
     * 
     * @param companyId the company ID
     * @param amount the amount to add to the voucher
     * @param dailyLimit the daily spending limit (used only if creating a new voucher)
     * @param startDate the start date (used only if creating a new voucher)
     * @param endDate the end date (used only if creating a new voucher)
     * @return the updated or created voucher
     */
    Voucher fundVoucher(Long companyId, BigDecimal amount, 
                        BigDecimal dailyLimit, LocalDate startDate, LocalDate endDate);

    /**
     * Fund an existing staff voucher or create a new one if it doesn't exist
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @param amount the amount to add to the voucher
     * @param dailyLimit the daily spending limit (used only if creating a new voucher)
     * @param startDate the start date (used only if creating a new voucher)
     * @param endDate the end date (used only if creating a new voucher)
     * @return the updated or created voucher
     */
    Voucher fundStaffVoucher(Long companyId, Long staffId, BigDecimal amount, 
                            BigDecimal dailyLimit, LocalDate startDate, LocalDate endDate);

    /**
     * Fund existing staff vouchers or create new ones if they don't exist
     * 
     * @param companyId the company ID
     * @param staffIds the list of staff IDs
     * @param amount the amount to add to each voucher
     * @param dailyLimit the daily spending limit (used only if creating new vouchers)
     * @param startDate the start date (used only if creating new vouchers)
     * @param endDate the end date (used only if creating new vouchers)
     * @return the list of updated or created vouchers
     */
    List<Voucher> fundStaffVouchers(Long companyId, List<Long> staffIds, BigDecimal amount, 
                                   BigDecimal dailyLimit, LocalDate startDate, LocalDate endDate);

    /**
     * Get a voucher for a company
     * 
     * @param companyId the company ID
     * @return the voucher if found, null otherwise
     */
    Voucher getVoucherByCompany(Long companyId);

    /**
     * Get a voucher for a staff member
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @return the voucher if found, null otherwise
     */
    Voucher getVoucherByStaff(Long companyId, Long staffId);

    /**
     * Get vouchers for multiple staff members
     * 
     * @param companyId the company ID
     * @param staffIds the list of staff IDs
     * @return the list of vouchers found
     */
    List<Voucher> getVouchersByStaffList(Long companyId, List<Long> staffIds);

    /**
     * Get all vouchers for a company
     * 
     * @param companyId the company ID
     * @param pageable pagination information
     * @return a page of vouchers
     */
    Page<Voucher> getVouchersByCompany(Long companyId, Pageable pageable);

    /**
     * Get all vouchers for a staff member
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @param pageable pagination information
     * @return a page of vouchers
     */
    Page<Voucher> getVouchersByStaff(Long companyId, Long staffId, Pageable pageable);

    /**
     * Check if a company can spend a certain amount using their voucher
     * 
     * @param companyId the company ID
     * @param amount the amount to check
     * @return true if the company can spend the amount, false otherwise
     */
    boolean canCompanySpend(Long companyId, BigDecimal amount);

    /**
     * Check if a staff member can spend a certain amount using their voucher
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @param amount the amount to check
     * @return true if the staff member can spend the amount, false otherwise
     */
    boolean canStaffSpend(Long companyId, Long staffId, BigDecimal amount);

    /**
     * Use a voucher to spend a certain amount
     * 
     * @param companyId the company ID
     * @param amount the amount to spend
     * @return the updated voucher
     * @throws IllegalArgumentException if the company cannot spend the amount
     */
    Voucher useVoucher(Long companyId, BigDecimal amount);

    /**
     * Use a staff voucher to spend a certain amount
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @param amount the amount to spend
     * @return the updated voucher
     * @throws IllegalArgumentException if the staff member cannot spend the amount
     */
    Voucher useStaffVoucher(Long companyId, Long staffId, BigDecimal amount);

    /**
     * Disable a voucher
     * 
     * @param companyId the company ID
     * @return the disabled voucher
     */
    Voucher disableVoucher(Long companyId);

    /**
     * Disable a staff voucher
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @return the disabled voucher
     */
    Voucher disableStaffVoucher(Long companyId, Long staffId);

    /**
     * Disable vouchers for multiple staff members
     * 
     * @param companyId the company ID
     * @param staffIds the list of staff IDs
     * @return the list of disabled vouchers
     */
    List<Voucher> disableStaffVouchers(Long companyId, List<Long> staffIds);

    /**
     * Enable a voucher
     * 
     * @param companyId the company ID
     * @return the enabled voucher
     */
    Voucher enableVoucher(Long companyId);

    /**
     * Enable a staff voucher
     * 
     * @param companyId the company ID
     * @param staffId the staff ID
     * @return the enabled voucher
     */
    Voucher enableStaffVoucher(Long companyId, Long staffId);

    /**
     * Enable vouchers for multiple staff members
     * 
     * @param companyId the company ID
     * @param staffIds the list of staff IDs
     * @return the list of enabled vouchers
     */
    List<Voucher> enableStaffVouchers(Long companyId, List<Long> staffIds);
}
