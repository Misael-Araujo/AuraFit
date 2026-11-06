package com.aurafit.trustsafety.application;

import java.util.UUID;

public interface IdentityVerificationPort {
  boolean isOwnerVerified(UUID userId);
}
