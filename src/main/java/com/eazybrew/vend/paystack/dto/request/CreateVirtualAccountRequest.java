package com.eazybrew.vend.paystack.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class CreateVirtualAccountRequest {
    @JsonProperty("preferred_bank")
    private String preferredBank;
    @JsonProperty("customer")
    private String customer;
}
