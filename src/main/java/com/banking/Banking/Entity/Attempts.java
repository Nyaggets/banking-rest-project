package com.banking.Banking.Entity;

import java.time.Instant;

/**
 * Запись для хранения количества попыток подтверждения личности и времени ожидания
 */
public record Attempts(Integer attemptsLeft, Instant expiresAt) {
    public Attempts(Integer attemptsLeft, Instant expiresAt) {
        this.attemptsLeft = attemptsLeft;
        this.expiresAt = expiresAt;
    }
}
