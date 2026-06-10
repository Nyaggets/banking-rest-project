package com.banking.Banking.validation;

import lombok.Getter;

import java.util.Map;

@Getter
public class CustomException extends RuntimeException {
    private final String errorCode;
    private final Map<String, String> errors;

    public CustomException(String errorCode, Map<String, String> errors) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errors = errors;
    }
}
