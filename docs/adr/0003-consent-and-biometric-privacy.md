# ADR 0003: Consent and biometric-data privacy

- **Status:** Accepted
- **Decision:** Treat face photos and derived avatar models as sensitive personal data. Record consent per purpose, policy version, grant/revocation, and timestamp in append-only rows. Require valid consent before relevant media/AI use cases. Never train models on user media. Plan hard deletion of source and derived data and configurable raw-image retention.
- **Context:** The product processes biometric data and is subject to LGPD duties; consent and deletion cannot be retrofitted safely.
- **Consequences:** Every processing use case must check current consent. Future upload and job workflows must define a durable deletion strategy spanning database, object storage, provider retention, and audit evidence. Age is gated at 18+ in MVP.
- **Alternatives:** One blanket consent (rejected as insufficiently granular); mutable consent row (rejected because it loses revocation history).
