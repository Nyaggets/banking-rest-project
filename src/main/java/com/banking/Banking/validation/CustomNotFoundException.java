package com.banking.Banking.validation;

import lombok.Getter;

@Getter
public class CustomNotFoundException extends RuntimeException {
    private final String errorField;

    public CustomNotFoundException(String message, String field) {
        super(message);
        this.errorField = field;
    }
}
