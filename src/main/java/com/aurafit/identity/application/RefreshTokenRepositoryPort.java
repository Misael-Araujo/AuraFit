package com.aurafit.identity.application;

import com.aurafit.identity.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

  Optional<RefreshToken> findForUpdateByTokenHash(String tokenHash);

  RefreshToken save(RefreshToken token);

  void revokeFamily(UUID familyId, Instant revokedAt);
}
