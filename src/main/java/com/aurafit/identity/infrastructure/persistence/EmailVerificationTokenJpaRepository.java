package com.aurafit.identity.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EmailVerificationTokenJpaRepository
    extends JpaRepository<EmailVerificationTokenEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select token from EmailVerificationTokenEntity token where token.tokenHash = :hash")
  Optional<EmailVerificationTokenEntity> findForUpdateByTokenHash(@Param("hash") String hash);
}
