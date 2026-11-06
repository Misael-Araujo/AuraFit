package com.aurafit.avatar.web;

import com.aurafit.avatar.domain.Avatar;
import java.time.Instant;
import java.util.UUID;

public record AvatarView(
    UUID id,
    UUID ownerUserId,
    String status,
    UUID sourceMediaId,
    UUID resultMediaId,
    String version,
    Instant createdAt,
    Instant updatedAt) {
  public static AvatarView of(Avatar avatar) {
    return new AvatarView(
        avatar.id(),
        avatar.ownerUserId(),
        avatar.status().name(),
        avatar.sourceMediaId(),
        avatar.resultMediaId(),
        avatar.version(),
        avatar.createdAt(),
        avatar.updatedAt());
  }
}
