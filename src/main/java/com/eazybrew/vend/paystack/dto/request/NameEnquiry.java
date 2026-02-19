package com.eazybrew.vend.paystack.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NameEnquiry {
    @NotNull(message = "Pass Code")
    @NotEmpty(message = "Pass Code")
    private String code;
    @NotNull(message = "Pass account number")
    @NotEmpty(message = "Pass account number")
    private String accountNumber;
}
