# ADR 0004: Reliable events and asynchronous AI jobs

- **Status:** Accepted
- **Decision:** Use Spring Modulith's JPA event publication registry for reliable cross-module event delivery. AI work is represented by a `GenerationJob` state machine and submitted through a `JobQueuePort`; begin with a PostgreSQL-backed worker and keep RabbitMQ/SQS as swappable adapters. Provider calls use application ports, bounded timeouts, retries/backoff, and circuit breakers.
- **Context:** AI calls are long-running, costly, and can fail independently of API requests. Cross-module side effects must survive process failure.
- **Consequences:** Events must not contain secrets or raw biometric data. Job processing needs idempotency, quotas, retry limits, dead-letter handling, and per-user authorization. The event registry dependency is present in Phase 1; provider adapters and job execution are deferred to Phase 2.
- **Alternatives:** In-process fire-and-forget events (rejected as lossy); introducing a broker immediately (deferred until throughput justifies operations); direct synchronous AI request (rejected due latency and cost).
