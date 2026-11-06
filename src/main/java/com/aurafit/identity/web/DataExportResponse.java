package com.aurafit.identity.web;

import java.time.Instant;
import java.util.UUID;

public record DataExportResponse(UUID id, String downloadUrl, Instant expiresAt) {}
