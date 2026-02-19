package com.eazybrew.vend.dto.response;

import com.eazybrew.vend.model.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponseDto {

    private String status;
    private String message;
    private PaymentStatusData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStatusData {
        private String transactionReference;
        private String productName;
        private String productDescription;
        private Double amount;
        private String status; // SUCCESS or FAILED
        private PaymentType paymentType;
    }
}
