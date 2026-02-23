package com.eazybrew.vend.paystack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitializeTransactionRequest {
    private String email;
    private Long amount;
    private String callback_url;
}