package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.Voucher;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.VoucherRepository;
import com.eazybrew.vend.service.StaffService;
import com.eazybrew.vend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final CompanyRepository companyRepository;
    private final StaffService staffService;

    @Override
    @Transactional
    public Voucher createStaffVoucher(Long companyId, Long staffId, BigDecimal dailyLimit,
                                     BigDecimal totalAmount, LocalDate startDate, LocalDate endDate) {
        // Validate inputs
        if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Daily limit must be positive", HttpStatus.BAD_REQUEST);
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Total amount must be positive", HttpStatus.BAD_REQUEST);
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            throw new CustomException("End date is required", HttpStatus.BAD_REQUEST);
        }
        if (endDate.isBefore(startDate)) {
            throw new CustomException("End date cannot be before start date", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get staff
        Staff staff = staffService.getStaffById(companyId, staffId);
        if (staff == null) {
            throw new CustomException("Staff not found", HttpStatus.NOT_FOUND);
        }

        // Check if voucher already exists for this staff
        Optional<Voucher> existingVoucher = voucherRepository.findByStaffAndCompanyAndRecordStatus(
                staff, company, RecordStatusConstant.ACTIVE);
        if (existingVoucher.isPresent()) {
            throw new CustomException("Voucher already exists for this staff member", HttpStatus.CONFLICT);
        }

        // Create new voucher
        Voucher voucher = Voucher.builder()
                .dailyLimit(dailyLimit)
                .totalAmount(totalAmount)
                .usedAmountToday(BigDecimal.ZERO)
                .lastResetDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .company(company)
                .staff(staff)
                .enabled(true)
                .build();

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public List<Voucher> createStaffVouchers(Long companyId, List<Long> staffIds, BigDecimal dailyLimit,
                                           BigDecimal totalAmount, LocalDate startDate, LocalDate endDate) {
        // Validate inputs
        if (staffIds == null || staffIds.isEmpty()) {
            throw new CustomException("Staff IDs list cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Daily limit must be positive", HttpStatus.BAD_REQUEST);
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Total amount must be positive", HttpStatus.BAD_REQUEST);
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            throw new CustomException("End date is required", HttpStatus.BAD_REQUEST);
        }
        if (endDate.isBefore(startDate)) {
            throw new CustomException("End date cannot be before start date", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Create vouchers for each staff member
        List<Voucher> vouchers = new java.util.ArrayList<>();
        for (Long staffId : staffIds) {
            try {
                // Get staff
                Staff staff = staffService.getStaffById(companyId, staffId);
                if (staff == null) {
                    log.warn("Staff with ID {} not found, skipping", staffId);
                    continue;
                }

                // Check if voucher already exists for this staff
                Optional<Voucher> existingVoucher = voucherRepository.findByStaffAndCompanyAndRecordStatus(
                        staff, company, RecordStatusConstant.ACTIVE);
                if (existingVoucher.isPresent()) {
                    log.warn("Voucher already exists for staff with ID {}, skipping", staffId);
                    continue;
                }

                // Create new voucher
                Voucher voucher = Voucher.builder()
                        .dailyLimit(dailyLimit)
                        .totalAmount(totalAmount)
                        .usedAmountToday(BigDecimal.ZERO)
                        .lastResetDate(LocalDate.now())
                        .startDate(startDate)
                        .endDate(endDate)
                        .company(company)
                        .staff(staff)
                        .enabled(true)
                        .build();

                vouchers.add(voucherRepository.save(voucher));
            } catch (Exception e) {
                log.error("Error creating voucher for staff with ID {}: {}", staffId, e.getMessage());
                // Continue with next staff member
            }
        }

        return vouchers;
    }

    @Override
    @Transactional
    public Voucher createVoucher(Long companyId, BigDecimal dailyLimit,
                                 BigDecimal totalAmount, LocalDate startDate, LocalDate endDate) {
        // Validate inputs
        if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Daily limit must be positive", HttpStatus.BAD_REQUEST);
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Total amount must be positive", HttpStatus.BAD_REQUEST);
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            throw new CustomException("End date is required", HttpStatus.BAD_REQUEST);
        }
        if (endDate.isBefore(startDate)) {
            throw new CustomException("End date cannot be before start date", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Check if voucher already exists
        List<Voucher> existingVouchers = voucherRepository.findByCompanyAndRecordStatusAndDateBetweenStartAndEnd(
                company, RecordStatusConstant.ACTIVE, startDate);
        if (!existingVouchers.isEmpty()) {
            throw new CustomException("Voucher already exists for this company for this period", HttpStatus.CONFLICT);
        }

        // Create new voucher
        Voucher voucher = Voucher.builder()
                .dailyLimit(dailyLimit)
                .totalAmount(totalAmount)
                .usedAmountToday(BigDecimal.ZERO)
                .lastResetDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .company(company)
                .enabled(true)
                .build();

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher fundStaffVoucher(Long companyId, Long staffId, BigDecimal amount,
                                   BigDecimal dailyLimit, LocalDate startDate, LocalDate endDate) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Funding amount must be positive", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get staff
        Staff staff = staffService.getStaffById(companyId, staffId);
        if (staff == null) {
            throw new CustomException("Staff not found", HttpStatus.NOT_FOUND);
        }

        // Check if voucher exists
        Optional<Voucher> existingVoucherOpt = voucherRepository.findByStaffAndCompanyAndRecordStatus(
                staff, company, RecordStatusConstant.ACTIVE);

        Voucher voucher;
        if (existingVoucherOpt.isPresent()) {
            // Update existing voucher
            voucher = existingVoucherOpt.get();
            voucher.setTotalAmount(voucher.getTotalAmount().add(amount));

            // If voucher was disabled, enable it
            if (!voucher.isEnabled()) {
                voucher.setEnabled(true);
            }

            // If voucher has expired, extend it
            LocalDate today = LocalDate.now();
            if (voucher.getEndDate().isBefore(today)) {
                if (endDate != null && !endDate.isBefore(today)) {
                    voucher.setEndDate(endDate);
                } else {
                    // Default to 30 days from now if no valid end date provided
                    voucher.setEndDate(today.plusDays(30));
                }
                voucher.setStartDate(today);
            }
        } else {
            // Create new voucher
            if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("Daily limit must be positive for new voucher", HttpStatus.BAD_REQUEST);
            }
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            if (endDate == null) {
                // Default to 30 days from start date
                endDate = startDate.plusDays(30);
            }
            if (endDate.isBefore(startDate)) {
                throw new CustomException("End date cannot be before start date", HttpStatus.BAD_REQUEST);
            }

            voucher = Voucher.builder()
                    .dailyLimit(dailyLimit)
                    .totalAmount(amount)
                    .usedAmountToday(BigDecimal.ZERO)
                    .lastResetDate(LocalDate.now())
                    .startDate(startDate)
                    .endDate(endDate)
                    .company(company)
                    .staff(staff)
                    .enabled(true)
                    .build();
        }

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public List<Voucher> fundStaffVouchers(Long companyId, List<Long> staffIds, BigDecimal amount,
                                          BigDecimal dailyLimit, LocalDate startDate, LocalDate endDate) {
        // Validate inputs
        if (staffIds == null || staffIds.isEmpty()) {
            throw new CustomException("Staff IDs list cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Funding amount must be positive", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Fund vouchers for each staff member
        List<Voucher> vouchers = new java.util.ArrayList<>();
        for (Long staffId : staffIds) {
            try {
                // Fund individual staff voucher
                Voucher voucher = fundStaffVoucher(companyId, staffId, amount, dailyLimit, startDate, endDate);
                vouchers.add(voucher);
            } catch (Exception e) {
                log.error("Error funding voucher for staff with ID {}: {}", staffId, e.getMessage());
                // Continue with next staff member
            }
        }

        return vouchers;
    }

    @Override
    @Transactional
    public Voucher fundVoucher(Long companyId, BigDecimal amount,
                               BigDecimal dailyLimit, LocalDate startDate, LocalDate endDate) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Funding amount must be positive", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Check if voucher exists
        List<Voucher> existingVouchers = voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE);

        Voucher voucher;
        if (!existingVouchers.isEmpty()) {
            // Update existing voucher - use the first active voucher
            voucher = existingVouchers.get(0);
            voucher.setTotalAmount(voucher.getTotalAmount().add(amount));

            // If voucher was disabled, enable it
            if (!voucher.isEnabled()) {
                voucher.setEnabled(true);
            }

            // If voucher has expired, extend it
            LocalDate today = LocalDate.now();
            if (voucher.getEndDate().isBefore(today)) {
                if (endDate != null && !endDate.isBefore(today)) {
                    voucher.setEndDate(endDate);
                } else {
                    // Default to 30 days from now if no valid end date provided
                    voucher.setEndDate(today.plusDays(30));
                }
                voucher.setStartDate(today);
            }
        } else {
            // Create new voucher
            if (dailyLimit == null || dailyLimit.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("Daily limit must be positive for new voucher", HttpStatus.BAD_REQUEST);
            }
            if (startDate == null) {
                startDate = LocalDate.now();
            }
            if (endDate == null) {
                // Default to 30 days from start date
                endDate = startDate.plusDays(30);
            }
            if (endDate.isBefore(startDate)) {
                throw new CustomException("End date cannot be before start date", HttpStatus.BAD_REQUEST);
            }

            voucher = Voucher.builder()
                    .dailyLimit(dailyLimit)
                    .totalAmount(amount)
                    .usedAmountToday(BigDecimal.ZERO)
                    .lastResetDate(LocalDate.now())
                    .startDate(startDate)
                    .endDate(endDate)
                    .company(company)
                    .enabled(true)
                    .build();
        }

        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher getVoucherByCompany(Long companyId) {
        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get voucher - return the first active voucher
        List<Voucher> vouchers = voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE);
        return vouchers.isEmpty() ? null : vouchers.get(0);
    }

    @Override
    public Voucher getVoucherByStaff(Long companyId, Long staffId) {
        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get staff
        Staff staff = staffService.getStaffById(companyId, staffId);
        if (staff == null) {
            throw new CustomException("Staff not found", HttpStatus.NOT_FOUND);
        }

        // Get voucher
        Optional<Voucher> voucher = voucherRepository.findByStaffAndCompanyAndRecordStatus(
                staff, company, RecordStatusConstant.ACTIVE);
        return voucher.orElse(null);
    }

    @Override
    public List<Voucher> getVouchersByStaffList(Long companyId, List<Long> staffIds) {
        // Validate inputs
        if (staffIds == null || staffIds.isEmpty()) {
            throw new CustomException("Staff IDs list cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get staff members
        List<Staff> staffList = new java.util.ArrayList<>();
        for (Long staffId : staffIds) {
            try {
                Staff staff = staffService.getStaffById(companyId, staffId);
                if (staff != null) {
                    staffList.add(staff);
                }
            } catch (Exception e) {
                log.warn("Error getting staff with ID {}: {}", staffId, e.getMessage());
                // Continue with next staff member
            }
        }

        if (staffList.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // Get vouchers
        return voucherRepository.findByStaffInAndCompanyAndRecordStatus(
                staffList, company, RecordStatusConstant.ACTIVE);
    }

    @Override
    public Page<Voucher> getVouchersByStaff(Long companyId, Long staffId, Pageable pageable) {
        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get staff
        Staff staff = staffService.getStaffById(companyId, staffId);
        if (staff == null) {
            throw new CustomException("Staff not found", HttpStatus.NOT_FOUND);
        }

        // Get vouchers
        return voucherRepository.findByStaffAndRecordStatus(staff, RecordStatusConstant.ACTIVE, pageable);
    }

    @Override
    public Page<Voucher> getVouchersByCompany(Long companyId, Pageable pageable) {
        // Get company
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Get vouchers
        return voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE, pageable);
    }

    @Override
    public boolean canCompanySpend(Long companyId, BigDecimal amount) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Get voucher
        Voucher voucher = getVoucherByCompany(companyId);
        if (voucher == null) {
            return false;
        }

        // Check if voucher is valid and can spend
        return voucher.isValid() && voucher.canSpendToday(amount);
    }

    @Override
    public boolean canStaffSpend(Long companyId, Long staffId, BigDecimal amount) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Get voucher
        Voucher voucher = getVoucherByStaff(companyId, staffId);
        if (voucher == null) {
            return false;
        }

        // Check if voucher is valid and can spend
        return voucher.isValid() && voucher.canSpendToday(amount);
    }

    @Override
    @Transactional
    public Voucher useVoucher(Long companyId, BigDecimal amount) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Amount must be positive", HttpStatus.BAD_REQUEST);
        }

        // Get voucher
        Voucher voucher = getVoucherByCompany(companyId);
        if (voucher == null) {
            throw new CustomException("No active voucher found for this company", HttpStatus.NOT_FOUND);
        }

        // Check if voucher is valid
        if (!voucher.isValid()) {
            throw new CustomException("Voucher is not valid (disabled or expired)", HttpStatus.BAD_REQUEST);
        }

        // Try to spend
        try {
            voucher.spend(amount);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Cannot spend requested amount: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Save and return updated voucher
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher useStaffVoucher(Long companyId, Long staffId, BigDecimal amount) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Amount must be positive", HttpStatus.BAD_REQUEST);
        }

        // Get voucher
        Voucher voucher = getVoucherByStaff(companyId, staffId);
        if (voucher == null) {
            throw new CustomException("No active voucher found for this staff member", HttpStatus.NOT_FOUND);
        }

        // Check if voucher is valid
        if (!voucher.isValid()) {
            throw new CustomException("Voucher is not valid (disabled or expired)", HttpStatus.BAD_REQUEST);
        }

        // Try to spend
        try {
            voucher.spend(amount);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Cannot spend requested amount: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Save and return updated voucher
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher disableVoucher(Long companyId) {
        // Get voucher
        Voucher voucher = getVoucherByCompany(companyId);
        if (voucher == null) {
            throw new CustomException("No active voucher found for this company", HttpStatus.NOT_FOUND);
        }

        // Disable voucher
        voucher.setEnabled(false);
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher disableStaffVoucher(Long companyId, Long staffId) {
        // Get voucher
        Voucher voucher = getVoucherByStaff(companyId, staffId);
        if (voucher == null) {
            throw new CustomException("No active voucher found for this staff member", HttpStatus.NOT_FOUND);
        }

        // Disable voucher
        voucher.setEnabled(false);
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public List<Voucher> disableStaffVouchers(Long companyId, List<Long> staffIds) {
        // Validate inputs
        if (staffIds == null || staffIds.isEmpty()) {
            throw new CustomException("Staff IDs list cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // Disable vouchers for each staff member
        List<Voucher> vouchers = new java.util.ArrayList<>();
        for (Long staffId : staffIds) {
            try {
                Voucher voucher = disableStaffVoucher(companyId, staffId);
                vouchers.add(voucher);
            } catch (Exception e) {
                log.error("Error disabling voucher for staff with ID {}: {}", staffId, e.getMessage());
                // Continue with next staff member
            }
        }

        return vouchers;
    }

    @Override
    @Transactional
    public Voucher enableVoucher(Long companyId) {
        // Get voucher
        Voucher voucher = getVoucherByCompany(companyId);
        if (voucher == null) {
            throw new CustomException("No active voucher found for this company", HttpStatus.NOT_FOUND);
        }

        // Enable voucher
        voucher.setEnabled(true);
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher enableStaffVoucher(Long companyId, Long staffId) {
        // Get voucher
        Voucher voucher = getVoucherByStaff(companyId, staffId);
        if (voucher == null) {
            throw new CustomException("No active voucher found for this staff member", HttpStatus.NOT_FOUND);
        }

        // Enable voucher
        voucher.setEnabled(true);
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public List<Voucher> enableStaffVouchers(Long companyId, List<Long> staffIds) {
        // Validate inputs
        if (staffIds == null || staffIds.isEmpty()) {
            throw new CustomException("Staff IDs list cannot be empty", HttpStatus.BAD_REQUEST);
        }

        // Enable vouchers for each staff member
        List<Voucher> vouchers = new java.util.ArrayList<>();
        for (Long staffId : staffIds) {
            try {
                Voucher voucher = enableStaffVoucher(companyId, staffId);
                vouchers.add(voucher);
            } catch (Exception e) {
                log.error("Error enabling voucher for staff with ID {}: {}", staffId, e.getMessage());
                // Continue with next staff member
            }
        }

        return vouchers;
    }
}
