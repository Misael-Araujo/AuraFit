package com.aurafit.identity.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserView(
    UUID id,
    String email,
    String displayName,
    LocalDate birthDate,
    boolean emailVerified,
    Instant createdAt) {}
