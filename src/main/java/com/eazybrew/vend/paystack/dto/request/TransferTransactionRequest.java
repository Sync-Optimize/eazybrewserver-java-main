package com.eazybrew.vend.paystack.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransferTransactionRequest {
    private String source;
    private String reason;
    private BigDecimal amount;
    private String recipient;

}
