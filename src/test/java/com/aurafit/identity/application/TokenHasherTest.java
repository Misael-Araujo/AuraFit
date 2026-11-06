package com.aurafit.identity.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHasherTest {

  @Test
  void hashesTheSameTokenDeterministicallyWithoutReturningTheToken() {
    String rawToken = "opaque-refresh-token";

    String firstHash = TokenHasher.hash(rawToken);

    assertThat(firstHash).hasSize(64).isNotEqualTo(rawToken);
    assertThat(TokenHasher.hash(rawToken)).isEqualTo(firstHash);
    assertThat(TokenHasher.hash("different-token")).isNotEqualTo(firstHash);
  }
}
