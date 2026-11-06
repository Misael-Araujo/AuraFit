# Phase 1 backlog

Only these stories are in scope for the first implementation phase.

## Register and verify an adult account

As a prospective user, I can register an account and verify my email so that only adults with a verified address can sign in.

```gherkin
Feature: Account registration and email verification
  Scenario: Register an eligible adult
    Given I provide a valid email, password, display name, and date of birth showing I am at least 18
    When I register
    Then my password is stored as a one-way encoded value
    And a single-use verification token is emailed
    And only the token hash and expiry are stored
  Scenario: Reject a minor
    Given my date of birth shows I am under 18
    When I register
    Then the API rejects the request without creating an account
  Scenario: Verify a valid email token
    Given my account has an unexpired unused verification token
    When I submit that token
    Then my email is marked verified
    And the token cannot be used again
  Scenario: Reject an expired or reused token
    Given my verification token has expired or was consumed
    When I submit it
    Then the API returns a stable token error without revealing internal details
```

## Authenticate and manage sessions

As a verified user, I can sign in, rotate my refresh token, log out, and retrieve my profile.

```gherkin
Feature: Stateless authentication
  Scenario: Sign in with a verified account
    Given my account is verified and my password is correct
    When I sign in
    Then I receive a short-lived signed access token and an opaque refresh token
    And only a hash of the refresh token is stored
  Scenario: Deny an unverified or invalid login
    Given my credentials are invalid or my email is not verified
    When I sign in
    Then no token is issued
    And the response contains only a stable client-safe error
  Scenario: Rotate refresh credentials
    Given I have an active refresh token
    When I refresh my session
    Then the old token is revoked and a new token is issued in the same family
  Scenario: Detect refresh-token reuse
    Given a refresh token has already been rotated
    When it is presented again
    Then every active token in that family is revoked
  Scenario: Limit brute-force attempts
    Given repeated authentication failures come from my client address
    When the configured threshold is exceeded
    Then the API temporarily throttles authentication attempts
  Scenario: Read my identity
    Given I have a valid access token
    When I request `/auth/me`
    Then I receive only the account identified by the token subject
```

## Record purpose-specific consent

As a user, I can grant or revoke consent for individual processing purposes and inspect the current decisions.

```gherkin
Feature: Consent records
  Scenario: Grant consent
    Given I am authenticated
    When I grant a purpose under a policy version
    Then an immutable record with purpose, timestamp, and policy version is appended
  Scenario: Revoke consent
    Given I previously granted a purpose
    When I revoke that purpose
    Then a new denial record is appended without deleting history
  Scenario: Prevent cross-account access
    Given I am authenticated as one user
    When I request or update consent
    Then only the principal from my validated token is used as the owner
```

## Platform foundation

As a maintainer, I can run the app with Docker Compose and rely on repeatable migrations and CI checks.

```gherkin
Feature: Local development and quality gates
  Scenario: Start local dependencies
    Given Docker Compose is available
    When I run `docker compose up --build`
    Then the API, PostgreSQL, private MinIO bucket, and Mailpit start with health checks
  Scenario: Validate the database
    Given a clean PostgreSQL database
    When the API starts
    Then Flyway creates the schema and Hibernate validates it without generating schema
  Scenario: Verify a change
    Given source code and tests
    When CI runs `./mvnw verify`
    Then formatting, Checkstyle, tests, module boundaries, and the coverage threshold are enforced
```
