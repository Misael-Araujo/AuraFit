package com.aurafit.consent.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aurafit.AuraFitApplication;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AuraFitApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConsentAuthorizationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void clearUsers() {
    jdbcTemplate.update("delete from app_user");
  }

  @Test
  void consentOwnerIsDerivedFromTheAuthenticatedPrincipal() throws Exception {
    UUID ownerId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();
    insertUser(ownerId, "owner@example.com");
    insertUser(otherId, "other@example.com");

    mockMvc
        .perform(
            put("/api/v1/consents/AVATAR_CREATION")
                .with(jwt().jwt(token -> token.subject(ownerId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"granted\":true,\"policyVersion\":\"2026-01\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(ownerId.toString()));

    mockMvc
        .perform(
            get("/api/v1/consents").with(jwt().jwt(token -> token.subject(otherId.toString()))))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void rejectsUnauthenticatedAccessWithProblemDetails() throws Exception {
    mockMvc
        .perform(get("/api/v1/consents"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.code").value("authentication_required"));
  }

  private void insertUser(UUID id, String email) {
    jdbcTemplate.update(
        "insert into app_user "
            + "(id, email, password_hash, display_name, birth_date, email_verified, role, created_at, version) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        id,
        email,
        "{bcrypt}unused",
        "Test User",
        java.time.LocalDate.of(1990, 1, 1),
        true,
        "USER",
        Instant.now(),
        0);
  }
}
