package com.banking.Banking.validation;

import lombok.Getter;

import java.time.Instant;

@Getter
public class RequestLimitException extends Exception {
    private final String message;
    private final Instant expiresAt;

    public RequestLimitException(String message, Instant expiresAt) {
        super(message);
        this.message = message;
        this.expiresAt = expiresAt;
    }
}
