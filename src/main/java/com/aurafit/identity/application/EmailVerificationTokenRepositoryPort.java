package com.aurafit.identity.application;

import com.aurafit.identity.domain.EmailVerificationToken;
import java.util.Optional;

public interface EmailVerificationTokenRepositoryPort {

  Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

  EmailVerificationToken save(EmailVerificationToken token);
}
