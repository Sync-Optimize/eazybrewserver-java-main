package com.eazybrew.vend.paystack.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateCustomerPayStackResponse {

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Data data;

    // Getters and Setters
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    // Inner class to represent the "data" field
    @Builder
    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Data {

        @JsonProperty("transactions")
        private List<Object> transactions;

        @JsonProperty("subscriptions")
        private List<Object> subscriptions;

        @JsonProperty("authorizations")
        private List<Object> authorizations;

        @JsonProperty("email")
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("integration")
        private int integration;

        @JsonProperty("domain")
        private String domain;

        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        @JsonProperty("customer_code")
        private String customerCode;

        @JsonProperty("risk_action")
        private String riskAction;

        @JsonProperty("id")
        private Long id;

        @JsonProperty("createdAt")
        private Date createdAt;

        @JsonProperty("updatedAt")
        private Date updatedAt;

        @JsonProperty("identified")
        private boolean identified;

        @JsonProperty("identifications")
        private Object identifications;

        // Getters and Setters
        public List<Object> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<Object> transactions) {
            this.transactions = transactions;
        }

        public List<Object> getSubscriptions() {
            return subscriptions;
        }

        public void setSubscriptions(List<Object> subscriptions) {
            this.subscriptions = subscriptions;
        }

        public List<Object> getAuthorizations() {
            return authorizations;
        }

        public void setAuthorizations(List<Object> authorizations) {
            this.authorizations = authorizations;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public int getIntegration() {
            return integration;
        }

        public void setIntegration(int integration) {
            this.integration = integration;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public String getCustomerCode() {
            return customerCode;
        }

        public void setCustomerCode(String customerCode) {
            this.customerCode = customerCode;
        }

        public String getRiskAction() {
            return riskAction;
        }

        public void setRiskAction(String riskAction) {
            this.riskAction = riskAction;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }

        public boolean isIdentified() {
            return identified;
        }

        public void setIdentified(boolean identified) {
            this.identified = identified;
        }

        public Object getIdentifications() {
            return identifications;
        }

        public void setIdentifications(Object identifications) {
            this.identifications = identifications;
        }
    }

    @Override
    public String toString() {
        return "CreateCustomerPayStackResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

