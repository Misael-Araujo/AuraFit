package com.aurafit.identity.infrastructure.persistence;

import com.aurafit.identity.application.RefreshTokenRepositoryPort;
import com.aurafit.identity.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

  private final RefreshTokenJpaRepository repository;

  RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<RefreshToken> findForUpdateByTokenHash(String tokenHash) {
    return repository.findForUpdateByTokenHash(tokenHash).map(RefreshTokenEntity::toDomain);
  }

  @Override
  public RefreshToken save(RefreshToken token) {
    return repository.save(RefreshTokenEntity.from(token)).toDomain();
  }

  @Override
  public void revokeFamily(UUID familyId, Instant revokedAt) {
    repository.revokeActiveFamily(familyId, revokedAt);
  }
}
