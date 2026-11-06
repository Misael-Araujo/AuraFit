package com.aurafit.trustsafety.infrastructure;

import com.aurafit.trustsafety.application.IdentityVerificationPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeIdentityVerificationAdapter implements IdentityVerificationPort {

  private final TrustSafetyProperties properties;

  public FakeIdentityVerificationAdapter(TrustSafetyProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isOwnerVerified(UUID userId) {
    if (!properties.identityVerification().enabled()) {
      return true;
    }
    return !properties.identityVerification().requireVerifiedOwner() || true;
  }
}
