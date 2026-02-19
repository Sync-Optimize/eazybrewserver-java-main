package com.eazybrew.vend.paystack.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    private boolean active;
    private String createdAt;
    private String currency;
    private String domain;
    private int id;
    private int integration;
    private String name;
    private String recipient_code;
    private String type;
    private String updatedAt;
    private boolean isDeleted;
    private Details details;

}

