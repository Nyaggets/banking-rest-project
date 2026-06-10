package com.banking.Banking.Service;

import com.banking.Banking.Entity.Attempts;
import com.banking.Banking.validation.RequestLimitException;
import lombok.Setter;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerifyIdentityService {
    @Setter
    private Clock clock;
    private Map<Long, Attempts> passwordMap = new ConcurrentHashMap<>();
    private final Integer PASSWORD_ATTEMPTS = 3;
    private final Duration passwordDuration = Duration.ofHours(1);

    public VerifyIdentityService(Clock clock, Map<Long, Attempts> passwordMap) {
        this.clock = clock;
        this.passwordMap = passwordMap;
    }

    public void throwIfPasswordAttemptLimit(Long clientId, Boolean isPasswordVerified) throws AccessDeniedException {
        Instant timeNow = clock.instant();
        Attempts passwordAttempts = passwordMap.compute(clientId, (id, current) -> {
            if (current == null || timeNow.isAfter(current.expiresAt()))
                return new Attempts(PASSWORD_ATTEMPTS, timeNow.plus(1, ChronoUnit.HOURS));
            return current;
        });

        if (passwordAttempts.attemptsLeft() == 0)
            throw new RequestLimitException("Лимит попыток исчерпан.", passwordAttempts.expiresAt());

        if (!isPasswordVerified) {
            passwordMap.computeIfPresent(clientId, (id, current) ->
                new Attempts(current.attemptsLeft() - 1, current.expiresAt()));
            throw new AccessDeniedException("Неверный пароль. Осталось попыток: " + passwordAttempts.attemptsLeft());
        }
    }
}
