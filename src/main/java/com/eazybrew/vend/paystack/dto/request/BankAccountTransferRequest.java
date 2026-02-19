package com.eazybrew.vend.paystack.dto.request;

import lombok.*;


@Data
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountTransferRequest {
    private String type;
    private String name;
    private String account_number;
    private String bank_code;
    private String currency;
}
