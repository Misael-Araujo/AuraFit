# Contributing

## Workflow

- Use GitHub Flow: short-lived branches and focused pull requests.
- Follow Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`, `build:`, `ci:`).
- Keep changes inside the approved delivery phase. Do not add roadmap placeholders.
- Update architecture/API/backlog documentation when a boundary or behavior changes.

## Development

- Use Java 21+ and the checked-in Maven Wrapper.
- Run `./mvnw verify` before opening a pull request. On Windows, use `.\mvnw.cmd verify`.
- Start local dependencies and the API with `docker compose up --build`.
- Add Flyway migrations for schema changes. Never rely on Hibernate schema generation.
- Add unit tests for domain/application behavior, web/security tests including ownership checks, and Testcontainers integration tests for persistence/provider boundaries.
- Do not commit `.env`, secrets, personal data, build outputs, or generated artifacts.

## Definition of Done

- Behavior follows the feature module's public contract and approved phase.
- Input, authorization, consent, failure, and replay cases are tested where relevant.
- API errors use RFC 9457 Problem Details with a stable code.
- `./mvnw verify` passes, including formatting, Checkstyle, architecture, and coverage.
- Documentation and migration instructions are current; no TODO placeholders, commented-out code, or unused dependencies remain.
