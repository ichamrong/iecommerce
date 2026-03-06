## Auth Module Test Coverage — Core Classes

This document tracks **core auth module classes** that must be covered by **unit tests** as part of the first iteration toward ~100% coverage for the module.

The lists below are organised by layer and focus on classes with non-trivial behaviour (business rules, orchestration, mapping, or security concerns).

### 1. Already covered (representative)

- **Application handlers (examples)**: `LoginUserHandler`, `LogoutHandler`, `RefreshTokenHandler`, `TenantSignupHandler`, `UpdateTenantStatusHandler`, `ChangePasswordHandler`, `ChangeCredentialsHandler`, `Enable2FAHandler`, `ForgotPasswordHandler`, `AdminCreateUserHandler`, `RevokeSessionHandler`, `ListUserSessionsHandler`.
- **Domain model (examples)**: `User`, `Tenant`, `TenantPreferences`, `Role`, `UserAccountFactory`, `LoginLockPolicy`, `LoginAttemptRecord`, `UserSession`.
- **Infrastructure & security (examples)**: `KeycloakIdentityService`, `TenantPersistenceMapper`, `TenantContextFilter`, `CustomAccessDeniedHandler`, `CustomAuthenticationEntryPoint`.
- **API layer (examples)**: `AuthController`, `TenantController`.

These classes already have focused unit tests under `src/test/java/com/chamrong/iecommerce/auth/**`.

### 2. Phase 1 — application layer targets

Core command/query handlers and services that require or benefit from dedicated unit tests:

- `com.chamrong.iecommerce.auth.application.command.user.RegisterUserHandler`
- `com.chamrong.iecommerce.auth.application.command.user.DisableUserHandler`
- `com.chamrong.iecommerce.auth.application.command.tenant.UpdateTenantPreferencesHandler`
- `com.chamrong.iecommerce.auth.application.PosService`
- `com.chamrong.iecommerce.auth.application.query.UserQueryHandler`
- `com.chamrong.iecommerce.auth.application.query.GetTenantPreferencesHandler`

These tests focus on **business rules, orchestration of domain ports and IDP**, and **event publication**.

### 3. Phase 1 — API layer targets

Controller and exception-mapping components that should be covered with Web MVC or focused unit tests:

- `com.chamrong.iecommerce.auth.api.UserController`
- `com.chamrong.iecommerce.auth.api.PosAuthApiController`
- `com.chamrong.iecommerce.auth.api.AuthExceptionHandler`

Objectives:

- Verify **status codes, response shapes, and validation behaviour**.
- Ensure **permission annotations** and **tenant guarding** are wired correctly at the controller boundary.

### 4. Phase 1 — infrastructure & technical components

Infrastructure classes with meaningful logic (rate limiting, login lock storage, messaging) that need unit tests:

- `com.chamrong.iecommerce.auth.infrastructure.ratelimit.IpRateLimitFilter`
- `com.chamrong.iecommerce.auth.infrastructure.lock.InMemoryLoginLockStore`
- `com.chamrong.iecommerce.auth.infrastructure.messaging.AuthEventKafkaProducer`

Objectives:

- Validate **rate-limiting decisions** and `429` responses for sensitive endpoints.
- Verify **login lock record lifecycle** (find/save/clear) and key structure.
- Ensure **Kafka events** are serialised and sent correctly, and that failures are **logged but non-fatal**.

