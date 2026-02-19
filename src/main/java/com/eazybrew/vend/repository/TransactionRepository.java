package com.eazybrew.vend.repository;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Device;
import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.Transaction;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<Transaction> findByPaystackReference(String paystackReference);
    Optional<Transaction> findByReferenceNumber(String transactionReference);
    boolean existsByTransactionId(String transactionId);
    boolean existsByReferenceNumber(String transactionRef);

    List<Transaction> findByCompany(Company company);

    Page<Transaction> findByCompany(Company company, Pageable pageable);
    Page<Transaction> findByCompanyAndStaff(Company company, Staff staff, Pageable pageable);

    List<Transaction> findByDevice(Device device);

    Page<Transaction> findByDevice(Device device, Pageable pageable);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByCompanyAndStatus(Company company, TransactionStatus status);

    List<Transaction> findByTransactionType(TransactionType type);

    List<Transaction> findByDateCreatedBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.company = ?1 AND t.dateCreated BETWEEN ?2 AND ?3")
    List<Transaction> findByCompanyAndDateRange(Company company, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.device = ?1 AND t.dateCreated BETWEEN ?2 AND ?3")
    List<Transaction> findByDeviceAndDateRange(Device device, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalAmount();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.company = ?1 AND t.status = 'COMPLETED'")
    BigDecimal getTotalAmountByCompany(Company company);
}
