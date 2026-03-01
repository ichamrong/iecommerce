# Security and Reliability Hardening

**Version:** 1.0  
**Purpose:** ASVS mapping, outbox/saga, idempotency, retry, and key management.  
**Reference:** SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md §4, §5; OWASP ASVS L1/L2.

---

## 1. OWASP ASVS mapping

### 1.1 Level 1 (all modules)

| Requirement | Implementation |
|-------------|----------------|
| Input validation | Bean validation (@Valid); bounds; type-safe params |
| Output encoding | No raw user input in HTML/JS; API returns JSON |
| Authentication | JWT (Keycloak); no default creds |
| Session | Stateless JWT; token expiry |
| Access control | Tenant + role; deny by default; TenantContext from JWT only |
| Cryptography | TLS; no custom crypto; strong algorithms |
| Error handling | No stack traces to client; generic messages; errorCode in body |
| Data protection | Sensitive data at rest; @Masked in logs |
| Communication | HTTPS only |
| Business logic | No bypass via parameter tampering; filterHash for cursor |

### 1.2 Level 2 (auth, payment, invoice, promotion, refund, staff)

| Requirement | Implementation |
|-------------|----------------|
| Stricter auth | MFA ready; password policy (Keycloak) |
| Account lockout | Auth + customer login lockout; thresholds documented |
| Idempotency | Payment intent; order confirm/ship/cancel; sale quotation; invoice create; refund |
| Rate limiting | RateLimitingFilter on auth and payment endpoints |
| Audit logging | Audit module; who, what, when, tenant for sensitive actions |
| Invoice signature | Ed25519; canonical form; verification API |
| Key rotation | Vault-ready; document rotation for signing keys and API keys |
| Webhook replay | Signature verification; store event id (WebhookDeduplicationPort); reject duplicate |

---

## 2. Tenant safety and IDOR

- **TenantContextFilter:** Sets tenant from JWT; blocks SUSPENDED/TERMINATED; GRACE read-only.
- **Every resource load:** Verify `resource.tenantId == TenantContext.getCurrentTenant()` or use repository `findByTenantIdAndId(...)`.
- **TenantGuard.requireSameTenant(entityTenantId, currentTenantId):** Throws (404/403) on mismatch; use after load in critical modules (sale, order, invoice, payment, promotion).
- **Never accept tenantId from request body** for scope (except SuperAdmin with separate auth).

---

## 3. Bank-grade reliability patterns

### 3.1 State machines

- **Order:** OrderStateMachine; transitions via domain methods; @Version for optimistic locking.
- **Payment:** Intent lifecycle (created → authorized → captured / failed); document in code.
- **Booking:** Status (reserved → confirmed → checked-in → checked-out / cancelled); formalize transitions.

### 3.2 Outbox pattern

- One outbox table per bounded context (order, sale, invoice, promotion, payment, inventory, customer).
- Write event in same transaction as aggregate change; relay scheduler publishes at least once.
- Retry with exponential backoff + jitter; max attempts then dead-letter or alert.

### 3.3 Saga orchestration

- **Sale:** SaleSagaOrchestrator, SaleSagaListener; compensation on failure.
- **Order:** OrderSagaState, steps, listener.
- **Auth:** Tenant provisioning saga.
- Compensation must be idempotent.

### 3.4 Idempotency store

- Payment intent: idempotency key; findByIdempotencyKey; return same response.
- Order confirm/ship/cancel: idempotency key or composite key.
- Invoice create/email: idempotency key.
- Webhook: event id stored; duplicate rejected.

### 3.5 Retry and circuit breaker

- Outbox relay: retry with backoff; jitter.
- External calls (Stripe, email, PDF): retry with backoff; Resilience4j circuit breaker for provider failures (optional but recommended).

---

## 4. Secure key management

- **Signing keys (invoice):** Store in secret manager (e.g. Vault); rotate per procedure; document rotation steps.
- **API keys (Stripe, etc.):** No hardcoding; env or secret manager; rotate on compromise.
- **Webhook secrets:** Per provider; verify signature on every webhook; reject invalid.

---

## 5. Attack surface and mitigation

| Threat | Mitigation |
|--------|------------|
| IDOR | TenantGuard; findByTenantIdAndId; never trust ID alone |
| Cursor tampering | filterHash in cursor; reject mismatch (400 INVALID_CURSOR_FILTER_MISMATCH) |
| Webhook replay | WebhookDeduplicationPort; store event id; reject duplicate |
| Brute force login | Account lockout; rate limit on login |
| Privilege escalation | RBAC from Keycloak; staff role checks |
| Invoice forgery | Digital signature; verification API; immutable after issue |
| Refund abuse | Idempotency; business rules (max amount, state checks) |

---

*End of SECURITY_AND_RELIABILITY_HARDENING.md*
