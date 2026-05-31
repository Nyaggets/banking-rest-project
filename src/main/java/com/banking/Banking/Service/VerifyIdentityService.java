package com.banking.Banking.Service;

import com.banking.Banking.Entity.UserAttempts;
import com.banking.Banking.validation.RequestLimitException;
import lombok.Setter;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class VerifyIdentityService {
    @Setter
    private Clock clock;
    private Map<Long, UserAttempts> revealCount = new ConcurrentHashMap<>();
    private final Integer DETAILS_REVEAL_ATTEMPTS = 3;

    public VerifyIdentityService(Clock clock, Map<Long, UserAttempts> revealCount) {
        this.clock = clock;
        this.revealCount = revealCount;
    }

    public void throwIfAttemptLimit(Long clientId, String password, Boolean isPasswordVerified) throws RequestLimitException, AccessDeniedException {
        Instant attemptTime = clock.instant();
        UserAttempts userAttempts = revealCount.compute(clientId, (id, current) -> {
            if (current == null || attemptTime.isAfter(current.expiresAt()))
                return new UserAttempts(DETAILS_REVEAL_ATTEMPTS, attemptTime.plus(1, ChronoUnit.HOURS));
            return current;
        });

        if (userAttempts.attemptsLeft() == 0)
            throw new RequestLimitException("Лимит попыток исчерпан.", userAttempts.expiresAt());

        if (!isPasswordVerified) {
            revealCount.computeIfPresent(clientId, (id, current) ->
                new UserAttempts(current.attemptsLeft() - 1, current.expiresAt()));
            throw new AccessDeniedException("Неверный пароль. Осталось попыток: " + userAttempts.attemptsLeft());
        }
    }
}
