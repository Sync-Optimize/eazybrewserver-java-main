package com.eazybrew.vend.service;

import com.eazybrew.vend.dto.request.TransactionRequest;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse processTransaction(TransactionRequest request);
    Page<Transaction> getTransactionsByCompany(Long companyId, Pageable pageable);
    Page<Transaction> getTransactionsByCompanyAndStaffId(Long companyId, Long staffId, Pageable pageable);
    Page<Transaction> getTransactions(Pageable pageable);
}
