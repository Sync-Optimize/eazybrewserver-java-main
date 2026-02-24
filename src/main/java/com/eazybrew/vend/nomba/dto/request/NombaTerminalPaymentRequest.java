package com.eazybrew.vend.nomba.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NombaTerminalPaymentRequest {
    private String merchantTxRef;
    private long amount;
    private String currency;
}
