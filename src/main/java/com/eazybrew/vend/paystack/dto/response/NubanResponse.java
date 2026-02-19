package com.eazybrew.vend.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NubanResponse {
    private boolean status;
    private String message;
    private DataContent data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class DataContent {
        private Bank bank;
        private String account_name;
        private String account_number;
        private boolean assigned;
        private String currency;
        private Object metadata;
        private boolean active;
        private long id;
        private String created_at;
        private String updated_at;
        private Assignment assignment;
        private Customer customer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bank {
        private String name;
        private int id;
        private String slug;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assignment {
        private int integration;
        private long assignee_id;
        private String assignee_type;
        private boolean expired;
        private String account_type;
        private String assigned_at;
        private Object expired_at;
        private Object assignment_expires_at;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private long id;
        private String first_name;
        private String last_name;
        private String email;
        private String customer_code;
        private String phone;
        private Object metadata;
        private String risk_action;
        private String international_format_phone;
    }
}

