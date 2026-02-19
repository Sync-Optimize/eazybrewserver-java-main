package com.eazybrew.vend.paystack.dto.response;


public class TransferTransactionResponse {
    private boolean status;
    private String message;
    private TransferData data;

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

    public TransferData getData() {
        return data;
    }

    public void setData(TransferData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TransferResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
