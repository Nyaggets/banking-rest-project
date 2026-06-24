package com.banking.Banking.Service;

import com.banking.Banking.Entity.Attempts;
import com.banking.Banking.validation.RequestLimitException;
import lombok.Setter;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Сервис обработки подтверждения личности
 */
@Service
public class VerifyIdentityService {
    @Setter
    private Clock clock;
    private Map<Long, Attempts> passwordMap;
    private final Integer PASSWORD_ATTEMPTS = 3;

    public VerifyIdentityService(Clock clock, Map<Long, Attempts> passwordMap) {
        this.clock = clock;
        this.passwordMap = passwordMap;
    }

    /**
     * Метод обработки ввода пароля и блокировки при достижении лимита
     */
    public void throwIfPasswordAttemptLimit(Long clientId, Boolean isPasswordVerified) throws AccessDeniedException {
        Instant timeNow = clock.instant();
        Attempts currentAttempts = passwordMap.get(clientId);

        if (currentAttempts != null && currentAttempts.expiresAt().isBefore(timeNow)) {
            passwordMap.remove(clientId);
            currentAttempts = null;
        }
        if (currentAttempts != null && currentAttempts.attemptsLeft() == 0)
            throw new RequestLimitException("Лимит попыток исчерпан.", currentAttempts.expiresAt());

        if (!isPasswordVerified) {
            passwordMap.merge(clientId,
                    new Attempts(PASSWORD_ATTEMPTS - 1, timeNow.plus(1, ChronoUnit.HOURS)),
                    (existing, newValue) -> new Attempts(existing.attemptsLeft() - 1, existing.expiresAt())
            );

            Attempts updated = passwordMap.get(clientId);
            throw new AccessDeniedException("Неверный пароль. Осталось попыток: " + updated.attemptsLeft());
        }
    }
}
