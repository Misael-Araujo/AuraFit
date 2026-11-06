package com.aurafit.trustsafety.web;

import java.time.Instant;
import java.util.UUID;

public record ReportView(
    UUID id,
    UUID reportedByUserId,
    String targetType,
    UUID targetId,
    String reason,
    String status,
    Instant createdAt) {}
