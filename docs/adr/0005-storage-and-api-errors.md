# ADR 0005: Storage abstraction and API error contract

- **Status:** Accepted
- **Decision:** Put S3-compatible object operations behind a storage port (MinIO locally, AWS S3 in production). Expose versioned REST resources under `/api/v1` and map errors centrally to RFC 9457 Problem Details with stable codes.
- **Context:** Storage providers and internal failure details must not leak into domain or client contracts.
- **Consequences:** Future uploads must use private buckets, per-user keys, short-lived presigned URLs, strict size/magic-byte/dimension checks, image re-encoding, EXIF stripping, and malware-scan hooks. Clients may rely on error codes, not implementation-specific exception text.
- **Alternatives:** Direct provider SDK use in domain code (rejected); ad-hoc error JSON (rejected).
