package com.banking.Banking.validation;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MultipleValidationException extends RuntimeException {
    private final HashMap<String, String> errors;

    public MultipleValidationException(HashMap<String, String> errors) {
        this.errors = errors;
    }
}
