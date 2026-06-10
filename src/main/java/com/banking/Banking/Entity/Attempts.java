package com.banking.Banking.Entity;

import java.time.Instant;

public record Attempts(Integer attemptsLeft, Instant expiresAt) {
    public Attempts(Integer attemptsLeft, Instant expiresAt) {
        this.attemptsLeft = attemptsLeft;
        this.expiresAt = expiresAt;
    }
}
