# Changelog

All notable changes to AuraFit are documented here.

## 0.1.0 - Unreleased

- Bootstrap the Spring Boot modular monolith and local PostgreSQL, MinIO, and Mailpit stack.
- Add adult registration, email verification, login, JWT access tokens, rotating refresh tokens, logout, and current-user API.
- Add append-only purpose-specific consent records and RFC 9457 API errors.
- Add Flyway baseline, security defaults, OpenAPI, architecture/backlog/ADR docs, and CI quality gates.
- Add the Phase 2A media pipeline: validated multipart image uploads, moderation hooks, S3/MinIO-backed object storage, presigned GET URLs, and API endpoints for media retrieval and deletion.
- Add the Phase 2B generation queue and Phase 2C avatar lifecycle scaffolding, including DB-backed job tracking, job ownership endpoints, and biometric-consent gating for avatar creation.
- Add the Phase 3 wardrobe slice: garment aggregate, scan submission flow, fake garment extraction adapter, versioned PATCH/DELETE ownership checks, and filterable wardrobe listing with Flyway-backed persistence.
- Add the Phase 5A LGPD/privacy slice: current-consent enforcement for biometric processing, account self-deletion with re-authentication, data-export ZIP generation delivered through private signed URLs, abuse report endpoints with admin review, and append-only audit logging.
