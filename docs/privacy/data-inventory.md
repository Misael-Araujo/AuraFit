# Data inventory and privacy controls

This document is a draft for legal review and is not legal advice.

## Categories

| Category | Examples | Purpose | Retention |
|---|---|---|---|
| Identity and access | email, password hash, role, verification state | account creation, authentication, support | active account + recovery period |
| Consent | purpose, granted state, policy version, timestamp | lawful basis and revocation history | until account deletion + audit retention |
| Media | face photo, avatar outputs, storage key, checksum | profile and biometric processing | short retention + deletion on revocation |
| Wardrobe | garment tags, notes, links, category metadata | wardrobe management | user-controlled lifecycle |
| Safety and compliance | report metadata, audit records | abuse handling and privacy accountability | minimum necessary and legal hold exceptions |

## Controls

- All media objects are stored under private user-scoped keys and accessed via short-lived presigned URLs.
- Biometrics require current consent under the active policy version before processing.
- Account export is generated in a private bucket and returned via a time-limited signed URL.
- Account deletion is verified by current password and email confirmation before irreversible removal.
- Safety events and privacy-relevant actions are appended to an immutable audit log.
