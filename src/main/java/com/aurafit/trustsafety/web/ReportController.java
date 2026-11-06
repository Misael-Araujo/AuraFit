package com.aurafit.trustsafety.web;

import com.aurafit.shared.error.ApiException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

  private final JdbcTemplate jdbcTemplate;

  public ReportController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostMapping("/reports")
  @ResponseStatus(HttpStatus.CREATED)
  public ReportView createReport(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReportRequest request) {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.fromString(jwt.getSubject());
    Instant createdAt = Instant.now();
    jdbcTemplate.update(
        "insert into content_report "
            + "(id, reported_by_user_id, target_type, target_id, reason, status, created_at) "
            + "values (?, ?, ?, ?, ?, ?, ?)",
        id,
        userId,
        request.targetType(),
        request.targetId(),
        request.reason(),
        "OPEN",
        createdAt);
    jdbcTemplate.update(
        "insert into audit_event "
            + "(id, user_id, event_type, event_details, created_at) values (?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        userId,
        "content_report_created",
        "{\"reportId\":\""
            + id
            + "\",\"targetType\":\""
            + request.targetType()
            + "\",\"targetId\":\""
            + request.targetId()
            + "\"}",
        createdAt);
    return new ReportView(
        id, userId, request.targetType(), request.targetId(), request.reason(), "OPEN", createdAt);
  }

  @PutMapping("/reports/{id}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public ReportView reviewReport(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody ReportReviewRequest request) {
    String status = request.status();
    if (!("OPEN".equalsIgnoreCase(status)
        || "REVIEWED".equalsIgnoreCase(status)
        || "RESOLVED".equalsIgnoreCase(status))) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "invalid_report_status",
          "Report status must be OPEN, REVIEWED, or RESOLVED.");
    }
    UUID reviewerId = UUID.fromString(jwt.getSubject());
    Instant reviewedAt = Instant.now();
    int updated =
        jdbcTemplate.update(
            "update content_report set status = ?, reviewed_by_user_id = ?, reviewed_at = ? where id = ?",
            status.toUpperCase(),
            reviewerId,
            reviewedAt,
            id);
    if (updated == 0) {
      throw new ApiException(
          HttpStatus.NOT_FOUND, "report_not_found", "The report could not be found.");
    }
    jdbcTemplate.update(
        "insert into audit_event "
            + "(id, user_id, event_type, event_details, created_at) values (?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        reviewerId,
        "content_report_reviewed",
        "{\"reportId\":\"" + id + "\",\"status\":\"" + status.toUpperCase() + "\"}",
        reviewedAt);
    return jdbcTemplate.queryForObject(
        "select reported_by_user_id, target_type, target_id, reason, status, "
            + "created_at from content_report where id = ?",
        (rs, rowNum) ->
            new ReportView(
                id,
                (UUID) rs.getObject("reported_by_user_id"),
                rs.getString("target_type"),
                (UUID) rs.getObject("target_id"),
                rs.getString("reason"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant()),
        id);
  }
}
