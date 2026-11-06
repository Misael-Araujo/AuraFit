package com.aurafit.trustsafety.infrastructure;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aurafit.trust-safety")
public record TrustSafetyProperties(
    @Valid Moderation moderation, @Valid IdentityVerification identityVerification) {

  public record Moderation(boolean enabled, boolean defaultApproval) {}

  public record IdentityVerification(boolean enabled, boolean requireVerifiedOwner) {}
}
