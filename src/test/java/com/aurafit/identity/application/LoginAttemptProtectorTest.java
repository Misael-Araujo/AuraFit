package com.aurafit.identity.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aurafit.shared.error.ApiException;
import org.junit.jupiter.api.Test;

class LoginAttemptProtectorTest {

  private final LoginAttemptProtector protector = new LoginAttemptProtector();

  @Test
  void temporarilyLocksAnEmailAndAddressAfterFiveFailures() {
    for (int attempt = 0; attempt < 5; attempt++) {
      protector.recordFailure("person@example.com", "127.0.0.1");
    }

    assertThatThrownBy(() -> protector.checkAllowed("person@example.com", "127.0.0.1"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Try again later");
    assertThatCode(() -> protector.checkAllowed("person@example.com", "127.0.0.2"))
        .doesNotThrowAnyException();
  }

  @Test
  void clearsFailedAttemptsAfterSuccessfulAuthentication() {
    for (int attempt = 0; attempt < 5; attempt++) {
      protector.recordFailure("person@example.com", "127.0.0.1");
    }

    protector.recordSuccess("person@example.com", "127.0.0.1");

    assertThatCode(() -> protector.checkAllowed("person@example.com", "127.0.0.1"))
        .doesNotThrowAnyException();
  }
}
