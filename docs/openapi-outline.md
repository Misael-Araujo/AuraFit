# OpenAPI outline (Phase 1)

The generated OpenAPI 3 document is served at `/v3/api-docs` and Swagger UI at `/swagger-ui.html` in local/development profiles only. API base path: `/api/v1`. Authentication uses `Authorization: Bearer <accessToken>`. Requests and responses use JSON unless noted.

## Schemas

- `RegisterRequest`: `email` (email, max 320), `password` (12–72 characters; max 72 UTF-8 bytes), `displayName` (1–80), `birthDate` (ISO date; caller must be at least 18).
- `RegistrationResponse`: message and non-secret user view; never includes password/token hashes or verification token.
- `VerifyEmailRequest`: single-use opaque `token`.
- `LoginRequest`: email and password.
- `AuthTokens`: `accessToken`, `refreshToken`, `tokenType` (`Bearer`), `accessTokenExpiresAt`.
- `RefreshTokenRequest`: refresh token in JSON body; refresh tokens are not accepted in URLs.
- `UserView`: id, email, display name, birth date, verification state, creation timestamp.
- `ConsentRecord`: id, purpose, granted, policy version, recorded timestamp.
- Error responses: RFC 9457 `application/problem+json` with stable `code`.

## Operations

| Method | Path | Auth | Success | Behavior |
|---|---|---|---|---|
| `POST` | `/auth/register` | Public | `201` | Create adult account and send verification email. |
| `POST` | `/auth/verify-email` | Public | `204` | Verify once with a hashed, expiring token. |
| `POST` | `/auth/login` | Public | `200` | Require verified email; return short-lived JWT and refresh token. |
| `POST` | `/auth/refresh` | Public | `200` | Rotate refresh token; reuse revokes its active family. |
| `POST` | `/auth/logout` | Bearer | `204` | Revoke the supplied refresh-token family owned by the caller. |
| `GET` | `/auth/me` | Bearer | `200` | Return the authenticated user's view. |
| `GET` | `/consents` | Bearer | `200` | Return current decision per purpose for the caller. |
| `PUT` | `/consents/{purpose}` | Bearer | `200` | Append a grant or revocation and policy version. |

`GET /actuator/health/liveness` and `/actuator/health/readiness` are operational probes, not versioned business resources. API docs are disabled in production.

## Error codes

`invalid_request`, `invalid_credentials`, `email_not_verified`, `age_restriction`, `email_already_registered`, `verification_token_invalid`, `refresh_token_invalid`, `refresh_token_reused`, `account_temporarily_locked`, `invalid_policy_version`, `account_not_found`, `rate_limit_exceeded`, and `internal_error`.

Validation errors return `400`; duplicate account registration returns `409`; invalid credentials return `401`; an unverified account returns `403`; expired/used verification tokens return `410`; throttling returns `429`. No response includes stack traces or persistence/provider details.

## Deferred resource groups

Later phases add `/media`, `/avatars`, `/jobs`, `/wardrobe`, `/wishlist`, and `/outfits`. Long-running generation requests use `202 Accepted`, a `Location: /api/v1/jobs/{id}` header, polling, and `Idempotency-Key`. They are intentionally absent from the Phase 1 runtime contract.
