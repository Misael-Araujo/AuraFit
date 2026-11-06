package com.aurafit.trustsafety.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ReportRequest(
    @NotBlank @Size(max = 32) String targetType,
    @NotNull UUID targetId,
    @NotBlank @Size(max = 255) String reason) {}
