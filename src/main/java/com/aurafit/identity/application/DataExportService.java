package com.aurafit.identity.application;

import com.aurafit.consent.application.ConsentRepositoryPort;
import com.aurafit.consent.domain.ConsentRecord;
import com.aurafit.identity.domain.User;
import com.aurafit.identity.web.DataExportResponse;
import com.aurafit.media.application.ObjectStoragePort;
import com.aurafit.shared.error.ApiException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataExportService {

  private final UserRepositoryPort users;
  private final ConsentRepositoryPort consentRepository;
  private final ObjectStoragePort objectStorage;
  private final JdbcTemplate jdbcTemplate;

  public DataExportService(
      UserRepositoryPort users,
      ConsentRepositoryPort consentRepository,
      ObjectStoragePort objectStorage,
      JdbcTemplate jdbcTemplate) {
    this.users = users;
    this.consentRepository = consentRepository;
    this.objectStorage = objectStorage;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional
  public DataExportResponse export(UUID userId) {
    User user =
        users
            .findById(userId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "account_not_found",
                        "The account no longer exists."));

    List<ConsentRecord> consents = consentRepository.findByUserId(userId);
    String exportKey = "users/%s/data-export/%s.zip".formatted(userId, UUID.randomUUID());
    byte[] zip = buildZip(user, consents);
    objectStorage.store(exportKey, zip, "application/zip");

    jdbcTemplate.update(
        "insert into audit_event (id, user_id, event_type, event_details, created_at) values (?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        userId,
        "data_export_requested",
        "{"
            + "\"exportKey\":\""
            + exportKey
            + "\","
            + "\"requestedAt\":\""
            + Instant.now()
            + "\""
            + "}",
        Instant.now());

    return new DataExportResponse(
        UUID.fromString(exportKey.substring(exportKey.lastIndexOf('/') + 1).replace(".zip", "")),
        objectStorage.generatePresignedGetUrl(exportKey, Duration.ofMinutes(15)),
        Instant.now().plus(Duration.ofMinutes(15)));
  }

  private byte[] buildZip(User user, List<ConsentRecord> consents) {
    String metadata =
        "{\n"
            + "  \"account\": {\n"
            + "    \"id\": \""
            + user.id()
            + "\",\n"
            + "    \"email\": \""
            + user.email()
            + "\",\n"
            + "    \"displayName\": \""
            + user.displayName()
            + "\",\n"
            + "    \"birthDate\": \""
            + user.birthDate()
            + "\",\n"
            + "    \"role\": \""
            + user.role()
            + "\",\n"
            + "    \"emailVerified\": "
            + user.emailVerified()
            + ",\n"
            + "    \"createdAt\": \""
            + user.createdAt()
            + "\"\n"
            + "  },\n"
            + "  \"consents\": [\n";

    StringBuilder builder = new StringBuilder(metadata);
    for (int i = 0; i < consents.size(); i++) {
      ConsentRecord consent = consents.get(i);
      builder
          .append("    {\n")
          .append("      \"purpose\": \"")
          .append(consent.purpose())
          .append("\",\n")
          .append("      \"granted\": ")
          .append(consent.granted())
          .append(",\n")
          .append("      \"policyVersion\": \"")
          .append(consent.policyVersion())
          .append("\",\n")
          .append("      \"recordedAt\": \"")
          .append(consent.recordedAt())
          .append("\"\n")
          .append("    }")
          .append(i + 1 < consents.size() ? "," : "")
          .append("\n");
    }
    builder.append("  ]\n}\n");

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(output)) {
      ZipEntry metadataEntry = new ZipEntry("metadata/account.json");
      zipOutputStream.putNextEntry(metadataEntry);
      zipOutputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
      zipOutputStream.closeEntry();
      ZipEntry readme = new ZipEntry("README.txt");
      zipOutputStream.putNextEntry(readme);
      zipOutputStream.write(
          "AuraFit personal data export. Files are private and auto-expire via presigned URLs.\n"
              .getBytes(StandardCharsets.UTF_8));
      zipOutputStream.closeEntry();
    } catch (IOException exception) {
      throw new IllegalStateException("Could not build data export archive.", exception);
    }
    return output.toByteArray();
  }
}
