package com.aurafit.identity.application;

import java.time.Instant;

public record AuthTokens(
    String accessToken, String refreshToken, String tokenType, Instant accessTokenExpiresAt) {}
