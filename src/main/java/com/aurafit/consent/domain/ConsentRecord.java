package com.aurafit.consent.domain;

import java.time.Instant;
import java.util.UUID;

public record ConsentRecord(
    UUID id,
    UUID userId,
    ConsentPurpose purpose,
    boolean granted,
    String policyVersion,
    Instant recordedAt) {}
