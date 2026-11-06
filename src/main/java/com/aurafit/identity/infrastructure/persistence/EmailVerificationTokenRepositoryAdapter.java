package com.aurafit.identity.infrastructure.persistence;

import com.aurafit.identity.application.EmailVerificationTokenRepositoryPort;
import com.aurafit.identity.domain.EmailVerificationToken;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepositoryPort {

  private final EmailVerificationTokenJpaRepository repository;

  EmailVerificationTokenRepositoryAdapter(EmailVerificationTokenJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<EmailVerificationToken> findByTokenHash(String tokenHash) {
    return repository
        .findForUpdateByTokenHash(tokenHash)
        .map(EmailVerificationTokenEntity::toDomain);
  }

  @Override
  public EmailVerificationToken save(EmailVerificationToken token) {
    return repository.save(EmailVerificationTokenEntity.from(token)).toDomain();
  }
}
