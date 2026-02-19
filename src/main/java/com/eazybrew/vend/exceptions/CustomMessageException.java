package com.eazybrew.vend.exceptions;

import org.springframework.http.HttpStatus;

public class CustomMessageException extends RuntimeException {
    /**
     * For serialization: if any changes is made to this class, update the
     * serialversionID
     */
    private static final long serialVersionUID = 1L;

    private String message;
    private HttpStatus status;

    public CustomMessageException(String message, HttpStatus status) {
        this.message = message.replace("[","").replace("]","");
        this.status = status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}