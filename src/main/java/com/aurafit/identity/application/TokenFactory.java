package com.aurafit.identity.application;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class TokenFactory {

  private final SecureRandom secureRandom = new SecureRandom();

  public String newToken() {
    byte[] token = new byte[32];
    secureRandom.nextBytes(token);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
  }
}
