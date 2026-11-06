package com.aurafit.avatar.domain;

import java.time.Instant;
import java.util.UUID;

public record Avatar(
    UUID id,
    UUID ownerUserId,
    AvatarStatus status,
    UUID sourceMediaId,
    UUID resultMediaId,
    String version,
    Instant createdAt,
    Instant updatedAt,
    String bodyMeasurements) {}
