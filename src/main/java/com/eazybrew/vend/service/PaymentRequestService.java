package com.eazybrew.vend.service;

import com.eazybrew.vend.dto.request.PaymentRequestDto;
import com.eazybrew.vend.dto.response.PaymentReturnDataResponseDto;
import com.eazybrew.vend.dto.response.PaymentStatusResponseDto;
import com.eazybrew.vend.dto.response.TransactionResponse;

public interface PaymentRequestService {
    
    /**
     * Process a payment request
     * 
     * @param request the payment request
     * @return the payment status response
     */
    TransactionResponse processPaymentRequest(PaymentRequestDto request);
    
    /**
     * Get the status of a payment request
     * 
     * @param transactionReference the transaction reference
     * @return the payment status response
     */
    TransactionResponse getPaymentStatus(String transactionReference);
    
    /**
     * Get the return data for a payment request
     * 
     * @param transactionReference the transaction reference
     * @return the payment return data response
     */
    PaymentReturnDataResponseDto getPaymentReturnData(String transactionReference);
}