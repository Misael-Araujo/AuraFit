# Retention and deletion table

| Data set | Default retention | Deletion trigger |
|---|---|---|
| App account and profile data | while account remains active; recovery window after closure | user deletion or legal hold exception |
| Refresh and email tokens | 30 days and expiry window | rotation, reuse detection, or account deletion |
| Consent records | retained as historical proof until account deletion | revocation and deletion, unless legal hold applies |
| Raw face photos | 30 days default or shorter by policy | consent revocation, avatar deletion, or account deletion |
| Avatar results | maintained only while active | regeneration archive cleanup or account deletion |
| Export ZIP | 15 minutes signed URL lifetime | expiry after download or cleanup job |
| Audit records | minimum necessary, keep as append-only evidence | legal and regulator obligations review |
