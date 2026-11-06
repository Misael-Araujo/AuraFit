# Copilot instructions for AuraFit

## Stack and scope

- Java 21+, latest stable GA Spring Boot at implementation time, Maven Wrapper, PostgreSQL, and Flyway. Use environment-backed `@ConfigurationProperties`; never generate the production schema with Hibernate.
- The product is one modular monolith, not microservices. Implement only the approved phase; later roadmap items are not permission to add placeholders.
- Keep dependencies used, pinned through managed versions where possible, and avoid vendor-specific AI code in domain/application packages.

## Architecture

- Organize code by feature module: `domain`, `application` (use cases and ports), `infrastructure` (adapters), and `web` (controllers/DTOs).
- Dependencies point inward. Modules communicate through public application APIs or domain events. Verify module boundaries.
- Keep domain rules out of controllers, use constructor injection, immutable DTOs/records, and never expose JPA entities.
- Use an event publication registry for durable cross-module events; jobs are asynchronous, idempotent, quota-aware, and behind a queue port.

## Security and privacy

- Stateless Spring Security/Nimbus JWT validation; short-lived access tokens and rotating, hashed, revocable refresh tokens with reuse detection. Use `DelegatingPasswordEncoder`; do not implement cryptography.
- Enforce `USER`/`ADMIN`, default deny, method security, and owner checks for every user resource. Validate input, rate-limit and lock down authentication, restrict CORS, set security headers, and return RFC 9457 errors with stable codes.
- Face images and avatar models are sensitive data. Require explicit granular consent before processing; never train on user photos. Minimize retention, encrypt data, and plan verified deletion of all derived objects.
- Validate uploads by size, magic bytes, and dimensions; re-encode, strip EXIF/GPS, use private per-user object keys, short-lived URLs, and malware/moderation hooks.
- Never commit secrets, log secrets/PII, or expose stack traces. Local secrets belong in ignored `.env`; commit only `.env.example`.

## Testing and quality

- Add tests with each behavior: unit tests for domain/application, MVC/security tests including IDOR, PostgreSQL/MinIO Testcontainers integration tests, and architecture verification.
- Use Given/When/Then test names. Maintain at least 80% JaCoCo line coverage for domain/application.
- `./mvnw verify` must pass, including Spotless (`google-java-format`), Checkstyle, tests, and coverage. `docker compose up --build` must start a working local stack.
- Prefer small, reviewable changes and fix the root cause. Do not add TODO placeholders, commented-out code, unused dependencies, or unrelated changes.

## API, docs, and delivery

- REST resources are versioned under `/api/v1`; use correct status codes, validation, pagination, and `202 + Location` for long jobs. Support `Idempotency-Key` for jobs.
- Keep OpenAPI, architecture/ADR, backlog, README, contributing guide, changelog, and CI security checks current with behavior.
- Use Conventional Commits and GitHub Flow; tests accompany code and changes stay phase-scoped.
