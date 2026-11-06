# ADR 0002: Stateless identity and token lifecycle

- **Status:** Accepted
- **Decision:** Use Spring Security/Nimbus OAuth2 Resource Server with short-lived issuer-validated JWT access tokens and opaque 256-bit refresh tokens. Store only SHA-256 hashes of refresh and email-verification tokens. Rotate refresh tokens on every use, bind them to a family, and revoke the family on detected reuse. Use Spring's delegating password encoder with BCrypt in this phase.
- **Context:** The mobile API is stateless, while refresh credentials need revocation, rotation, and reuse detection. Identity remains behind an application port so a managed IdP can replace it.
- **Consequences:** Access tokens remain usable until expiry after logout; refresh revocation stops future token issuance. HMAC signing keys must be high entropy, environment supplied, and rotated deliberately. Rate limits and current account lockout are instance-local and must move to shared state for multi-instance production.
- **Alternatives:** Long-lived JWTs (rejected); storing plaintext refresh tokens (rejected); custom token cryptography (rejected); delegated IdP (kept as a future adapter).
