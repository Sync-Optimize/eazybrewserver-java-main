package com.eazybrew.vend.repository;

import com.eazybrew.vend.model.PaymentRequest;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    
    /**
     * Find a payment request by its transaction reference and record status
     * 
     * @param transactionReference the transaction reference
     * @param recordStatus the record status
     * @return an Optional containing the payment request if found, or empty if not found
     */
    Optional<PaymentRequest> findByTransactionReferenceAndRecordStatus(String transactionReference, RecordStatusConstant recordStatus);
}