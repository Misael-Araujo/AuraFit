package com.aurafit.identity.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record User(
    UUID id,
    String email,
    String passwordHash,
    String displayName,
    LocalDate birthDate,
    boolean emailVerified,
    UserRole role,
    Instant createdAt) {

  public User verified() {
    return new User(id, email, passwordHash, displayName, birthDate, true, role, createdAt);
  }
}
