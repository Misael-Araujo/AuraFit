package com.aurafit.identity.application;

import com.aurafit.shared.error.ApiException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptProtector {

  private static final int MAX_FAILURES = 5;
  private static final Duration LOCKOUT = Duration.ofMinutes(15);

  private final Cache<String, AttemptState> attempts =
      Caffeine.newBuilder().maximumSize(100_000).expireAfterAccess(1, TimeUnit.HOURS).build();

  public void checkAllowed(String email, String clientAddress) {
    AttemptState state = attempts.getIfPresent(key(email, clientAddress));
    if (state != null
        && state.lockedUntil() != null
        && state.lockedUntil().isAfter(Instant.now())) {
      throw new ApiException(
          HttpStatus.TOO_MANY_REQUESTS,
          "account_temporarily_locked",
          "Too many failed sign-in attempts. Try again later.");
    }
  }

  public void recordFailure(String email, String clientAddress) {
    attempts
        .asMap()
        .compute(
            key(email, clientAddress),
            (key, existing) -> {
              AttemptState state = existing == null ? new AttemptState(0, null) : existing;
              int failures = state.failures() + 1;
              return new AttemptState(
                  failures, failures >= MAX_FAILURES ? Instant.now().plus(LOCKOUT) : null);
            });
  }

  public void recordSuccess(String email, String clientAddress) {
    attempts.invalidate(key(email, clientAddress));
  }

  private String key(String email, String clientAddress) {
    return email.trim().toLowerCase(java.util.Locale.ROOT) + "\u0000" + clientAddress;
  }

  private record AttemptState(int failures, Instant lockedUntil) {}
}
