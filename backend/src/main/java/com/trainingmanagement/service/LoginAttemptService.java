package com.trainingmanagement.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stops password guessing: after 5 wrong passwords for the same username,
 * further login attempts for that username are refused for 15 minutes.
 * A successful login clears the counter.
 *
 * Kept in memory (it resets when the server restarts), which is enough
 * for a small single-server application.
 */
@Service
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant lockedUntil) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    /** Minutes left in the lock, or 0 if the username may try to log in. */
    public long minutesLocked(String username) {
        Attempts a = attempts.get(key(username));
        if (a == null || a.lockedUntil() == null) {
            return 0;
        }
        Duration left = Duration.between(Instant.now(), a.lockedUntil());
        if (left.isNegative() || left.isZero()) {
            attempts.remove(key(username));
            return 0;
        }
        return Math.max(1, (left.toSeconds() + 59) / 60);
    }

    public void loginFailed(String username) {
        attempts.compute(key(username), (k, a) -> {
            int failures = (a == null ? 0 : a.failures()) + 1;
            Instant lockedUntil = failures >= MAX_FAILURES ? Instant.now().plus(LOCK_TIME) : null;
            return new Attempts(lockedUntil != null ? 0 : failures, lockedUntil);
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(key(username));
    }

    private String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
