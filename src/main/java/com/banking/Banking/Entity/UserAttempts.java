package com.banking.Banking.Entity;

import java.time.Instant;
import java.time.LocalTime;

public record UserAttempts(Integer attemptsLeft, Instant expiresAt) {
    public UserAttempts(Integer attemptsLeft, Instant expiresAt) {
        this.attemptsLeft = attemptsLeft;
        this.expiresAt = expiresAt;
    }
}
