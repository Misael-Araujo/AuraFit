package com.aurafit.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void createsAnImmutableVerifiedCopy() {
    User user =
        new User(
            UUID.randomUUID(),
            "user@example.com",
            "encoded",
            "User",
            LocalDate.of(1990, 1, 1),
            false,
            UserRole.USER,
            Instant.now());

    User verified = user.verified();

    assertThat(user.emailVerified()).isFalse();
    assertThat(verified.emailVerified()).isTrue();
    assertThat(verified.id()).isEqualTo(user.id());
    assertThat(verified.email()).isEqualTo(user.email());
    assertThat(verified.passwordHash()).isEqualTo(user.passwordHash());
    assertThat(verified.displayName()).isEqualTo(user.displayName());
    assertThat(verified.birthDate()).isEqualTo(user.birthDate());
    assertThat(verified.role()).isEqualTo(user.role());
    assertThat(verified.createdAt()).isEqualTo(user.createdAt());
  }
}
