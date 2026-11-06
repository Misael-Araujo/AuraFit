package com.aurafit.trustsafety.web;

import jakarta.validation.constraints.NotBlank;

public record ReportReviewRequest(@NotBlank String status) {}
