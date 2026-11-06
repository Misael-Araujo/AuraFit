package com.aurafit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuraFitContextTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void startsWithMigratedAndValidatedSchema() {
    assertThat(jdbcTemplate.queryForObject("select count(*) from app_user", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("select count(*) from event_publication", Integer.class))
        .isZero();
  }
}
