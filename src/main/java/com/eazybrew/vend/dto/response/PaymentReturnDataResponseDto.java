package com.eazybrew.vend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReturnDataResponseDto {
    
    private String status;
    private String message;
    private String data; // This would be a URL that can be loaded on a web view
}