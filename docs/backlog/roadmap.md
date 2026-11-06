# Deferred roadmap

These are not Phase 1 implementation stories.

- **Phase 2A — Media pipeline and moderation:** validate upload size, magic bytes, dimensions; re-encode and strip EXIF/GPS; store private per-user objects behind an S3-compatible port; moderate inputs before use; require explicit consent and identity checks before future avatar processing.
- **Phase 2B — Async generation jobs and avatar lifecycle:** enqueue avatar jobs through the DB-backed worker, track job state and retries, enforce quotas/idempotency, and manage avatar replacement and retention. This phase now includes the queue/job ownership endpoints plus the biometric-processing consent gate used before avatar generation begins.
- **Phase 3 — Wardrobe and wishlist:** garment extraction jobs; owned and wishlist garment CRUD; filtering, sorting, pagination, and resource-ownership checks. The implemented slice includes the `wardrobe` module, scan workflow, versioned patching, and fake extraction adapter, while the remote product page import flow remains intentionally deferred because it requires SSRF protections.
- **Phase 4 — Outfits and try-on:** outfit composition and saved renders; tags, occasions, seasons, favorites; moderation/provenance on generated output.
- **Phase 5A — LGPD and privacy controls:** self-service data export, verified account deletion, consent-version enforcement for biometric processing, abuse reporting and admin review, and append-only audit logging. The 5A slice is now partially implemented in the API and persistence layer.
- **Phase 5 — Hardening and LGPD:** export and verified deletion of all personal and derived data, retention enforcement, quotas, dashboards, load/security testing, optional RabbitMQ/SQS adapters.

Each phase adds acceptance criteria, migrations, tests, and endpoint contracts before implementation. No ML model training is in scope; external AI providers are orchestrated through ports and asynchronous jobs.
