# AuraFit

AuraFit is a Java/Spring REST API for a virtual wardrobe and try-on mobile app. The repository is a modular monolith; only Phase 1 (identity, consent, and platform foundation) is implemented.

## Run locally

Requirements: Docker Compose v2 and Java 21+ (only needed to run tests outside Docker).

```powershell
Copy-Item .env.example .env
docker compose up --build
```

The API is available at `http://localhost:8080`; health checks are under `/actuator/health`. OpenAPI and Swagger UI are at `/v3/api-docs` and `/swagger-ui.html`. Mailpit is at `http://localhost:8025`; MinIO console is at `http://localhost:9001`. Local credentials are development-only and must not be used in a deployed environment.

To run tests and all quality gates:

```powershell
.\mvnw.cmd verify
```

Flyway owns all schema changes. Hibernate is configured to validate the schema, not create or update it. Authentication requires a verified email; local verification messages can be opened in Mailpit.

## Project layout

```text
src/main/java/com/aurafit/
├── identity/{domain,application,infrastructure,web}
├── consent/{domain,application,infrastructure,web}
└── shared/{config,error,web}
src/main/resources/db/migration/
docs/{architecture.md,openapi-outline.md,adr/,backlog/}
.github/{copilot-instructions.md,workflows/}
```

## Architecture and roadmap

See [architecture](docs/architecture.md), [OpenAPI outline](docs/openapi-outline.md), [Phase 1 backlog](docs/backlog/phase-1.md), and [later phases](docs/backlog/roadmap.md). Decisions are recorded in [ADRs](docs/adr/). AI features are external-provider orchestration only; this project does not train ML models.

## Contributing

Follow [CONTRIBUTING.md](CONTRIBUTING.md) and the repository-specific [Copilot instructions](.github/copilot-instructions.md). Each behavior change needs tests, migrations where applicable, updated API/docs, and a passing `./mvnw verify`.