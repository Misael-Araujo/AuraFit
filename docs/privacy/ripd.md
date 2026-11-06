# DPIA draft: biometric and personal-data processing

## Summary

AuraFit handles adult account data, consent decisions, media uploads, and generated avatar/try-on outputs. The main risks are unauthorized access, data minimization, consent drift, and retention of biometric media.

## Processing purposes

- account creation and login
- biometric processing for avatar generation
- media validation and moderation
- privacy and security incident handling

## Risk assessment

- Risk: consent drift between policy versions. Mitigation: require re-consent for biometric-processing use cases under a new policy version.
- Risk: unauthorized secondary use of stored media. Mitigation: private object storage, presigned URL expiry, and user-scoped key paths.
- Risk: deletion gaps due to async jobs or event delays. Mitigation: use events for deletion cleanup plus audit evidence and retention sweeps.

## Residual risk

This draft identifies the key controls in scope for an MVP. Legal review should verify the retention schedule, lawful basis, and subject-rights procedure before production launch.
