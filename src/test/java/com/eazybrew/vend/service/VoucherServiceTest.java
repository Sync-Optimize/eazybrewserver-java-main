package com.eazybrew.vend.service;

import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.BaseEntity;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Voucher;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.VoucherRepository;
import com.eazybrew.vend.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Company company;
    private Voucher voucher;
    private final Long companyId = 1L;

    @BeforeEach
    void setUp() {
        // Setup test data
        company = Company.builder()
                .name("Test Company")
                .address("Test Address")
                .enabled(true)
                .build();
        // Set ID using reflection since it's managed by JPA
        try {
            java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(company, companyId);
        } catch (Exception e) {
            // Fallback if reflection fails
            // This is just for testing, in real app the ID is managed by JPA
        }

        voucher = Voucher.builder()
                .dailyLimit(new BigDecimal("2000.00"))
                .totalAmount(new BigDecimal("10000.00"))
                .usedAmountToday(BigDecimal.ZERO)
                .lastResetDate(LocalDate.now())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .company(company)
                .enabled(true)
                .build();
        // Set ID using reflection
        try {
            java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(voucher, 1L);
        } catch (Exception e) {
            // Fallback if reflection fails
        }
    }

    @Test
    void createVoucher_Success() {
        // Arrange
        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        when(voucherRepository.findByCompanyAndRecordStatusAndDateBetweenStartAndEnd(
                eq(company), eq(RecordStatusConstant.ACTIVE), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        // Act
        Voucher result = voucherService.createVoucher(
                companyId,
                new BigDecimal("2000.00"),
                new BigDecimal("10000.00"),
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("2000.00"), result.getDailyLimit());
        assertEquals(new BigDecimal("10000.00"), result.getTotalAmount());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void createVoucher_VoucherAlreadyExists() {
        // Arrange
        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        List<Voucher> existingVouchers = new ArrayList<>();
        existingVouchers.add(voucher);
        when(voucherRepository.findByCompanyAndRecordStatusAndDateBetweenStartAndEnd(
                eq(company), eq(RecordStatusConstant.ACTIVE), any(LocalDate.class)))
                .thenReturn(existingVouchers);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () ->
                voucherService.createVoucher(
                        companyId,
                        new BigDecimal("2000.00"),
                        new BigDecimal("10000.00"),
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                )
        );
        assertEquals("Voucher already exists for this company for this period", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void fundVoucher_ExistingVoucher() {
        // Arrange
        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        List<Voucher> existingVouchers = new ArrayList<>();
        existingVouchers.add(voucher);
        when(voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE))
                .thenReturn(existingVouchers);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        BigDecimal fundAmount = new BigDecimal("5000.00");
        BigDecimal expectedTotal = voucher.getTotalAmount().add(fundAmount);

        // Act
        Voucher result = voucherService.fundVoucher(
                companyId,
                fundAmount,
                null,
                null,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result.getTotalAmount());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void useVoucher_Success() {
        // Arrange
        BigDecimal spendAmount = new BigDecimal("1000.00");
        BigDecimal expectedRemaining = voucher.getTotalAmount().subtract(spendAmount);
        BigDecimal expectedUsedToday = voucher.getUsedAmountToday().add(spendAmount);

        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        List<Voucher> existingVouchers = new ArrayList<>();
        existingVouchers.add(voucher);
        when(voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE))
                .thenReturn(existingVouchers);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> {
            Voucher savedVoucher = invocation.getArgument(0);
            savedVoucher.setTotalAmount(expectedRemaining);
            savedVoucher.setUsedAmountToday(expectedUsedToday);
            return savedVoucher;
        });

        // Act
        Voucher result = voucherService.useVoucher(companyId, spendAmount);

        // Assert
        assertNotNull(result);
        assertEquals(expectedRemaining, result.getTotalAmount());
        assertEquals(expectedUsedToday, result.getUsedAmountToday());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void useVoucher_ExceedsDailyLimit() {
        // Arrange
        voucher.setUsedAmountToday(new BigDecimal("1500.00")); // Already used 1500
        BigDecimal spendAmount = new BigDecimal("1000.00"); // Trying to spend 1000 more (total 2500 > daily limit 2000)

        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        List<Voucher> existingVouchers = new ArrayList<>();
        existingVouchers.add(voucher);
        when(voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE))
                .thenReturn(existingVouchers);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () ->
                voucherService.useVoucher(companyId, spendAmount)
        );
        assertTrue(exception.getMessage().contains("Cannot spend requested amount"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void canCompanySpend_Success() {
        // Arrange
        BigDecimal spendAmount = new BigDecimal("1000.00");

        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        List<Voucher> existingVouchers = new ArrayList<>();
        existingVouchers.add(voucher);
        when(voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE))
                .thenReturn(existingVouchers);

        // Act
        boolean result = voucherService.canCompanySpend(companyId, spendAmount);

        // Assert
        assertTrue(result);
    }

    @Test
    void canCompanySpend_NoVoucher() {
        // Arrange
        BigDecimal spendAmount = new BigDecimal("1000.00");

        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        when(voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE))
                .thenReturn(new ArrayList<>());

        // Act
        boolean result = voucherService.canCompanySpend(companyId, spendAmount);

        // Assert
        assertFalse(result);
    }

    @Test
    void canCompanySpend_ExceedsDailyLimit() {
        // Arrange
        voucher.setUsedAmountToday(new BigDecimal("1500.00")); // Already used 1500
        BigDecimal spendAmount = new BigDecimal("1000.00"); // Trying to spend 1000 more (total 2500 > daily limit 2000)

        when(companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE))
                .thenReturn(Optional.of(company));
        List<Voucher> existingVouchers = new ArrayList<>();
        existingVouchers.add(voucher);
        when(voucherRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE))
                .thenReturn(existingVouchers);

        // Act
        boolean result = voucherService.canCompanySpend(companyId, spendAmount);

        // Assert
        assertFalse(result);
    }
}
