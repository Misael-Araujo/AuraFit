package com.aurafit.media.domain;

import java.time.Instant;
import java.util.UUID;

public record MediaAsset(
    UUID id,
    UUID ownerUserId,
    MediaKind kind,
    String storageKey,
    String originalFileName,
    String contentType,
    long sizeBytes,
    int width,
    int height,
    String sha256,
    ModerationStatus moderationStatus,
    Instant createdAt,
    Instant retentionUntil) {}
