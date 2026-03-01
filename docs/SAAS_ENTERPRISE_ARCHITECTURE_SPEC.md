# IECommerce API — SaaS Enterprise Architecture Specification

**Version:** 1.0  
**Status:** Authoritative reference for multi-vertical SaaS, security, and implementation  
**Repository:** `iecommerce-api` (Spring Boot 3.4.3, Java 21, Liquibase, PostgreSQL)

---

## Assumptions

- **Multi-tenancy:** Shared schema with `tenant_id` on all tenant-scoped tables; no schema-per-tenant.
- **Identity:** Keycloak as IdP; JWT carries `tenantId` and `sub` (user UUID); staff identity via `StaffSecurityContext.currentActorId()`.
- **Verticals:** E-commerce, POS, Accommodation (and hybrid) are first-class; Restaurant POS and Booking (appointments) are in roadmap.
- **Monetization:** Subscription plans (Free/Pro/Enterprise) with add-on modules and usage-based caps; billing via `subscription` + `payment` modules.
- **Compliance:** OWASP ASVS L1 everywhere; L2 for auth, payment, invoice, promotion, refund, staff.
- **Database:** PostgreSQL primary; MySQL differences noted where relevant.

---

# SECTION 1 — SaaS Business Model Analysis

## 1.1 Multi-Tenancy

| Aspect | Design | Current State | Gap |
|--------|--------|---------------|-----|
| **Schema** | Shared schema, `tenant_id` on every tenant-scoped entity | ✅ `BaseTenantEntity`, `tenant_id` in Liquibase (v1–v7+) | None |
| **Isolation** | All queries filter by `TenantContext.getCurrentTenant()` | ✅ `TenantContextFilter` sets tenant from JWT; `TenantContext.requireTenantId()` | Enforce at repository layer (some raw queries may omit tenant) |
| **Cross-tenant leakage** | No API may return another tenant’s data; IDs are tenant-scoped in authorization | Tenant from JWT; controllers use `TenantContext` | Add explicit IDOR checks: every resource access must verify `resource.tenantId == TenantContext.getCurrentTenant()` |
| **Tenant lifecycle** | TRIAL → ACTIVE → SUSPENDED → TERMINATED | ✅ `TenantSubscription` with `SubscriptionStatus` (TRIAL, ACTIVE, etc.); `Tenant` in auth | SUSPENDED/TERMINATED must block all API access in `TenantContextFilter` (validate tenant status) |
| **GDPR deletion** | Hard delete or anonymize PII; retain audit trail per regulation | Audit events exist; no formal GDPR export/deletion flow | Add: (1) Export API (JSON/CSV of user data), (2) Deletion workflow (anonymize + delete or hard-delete with audit), (3) Data processing agreement hooks |

**Tenant lifecycle (formal):**

- **Trial:** Time-limited; feature set per plan; no charge.
- **Active:** Billing enabled; full feature set per plan; quotas enforced.
- **Suspended:** Billing overdue or admin action; read-only or 4xx on all write APIs; grace period configurable.
- **Terminated:** Data retained for legal/audit; no login; optional purge after retention window.

**Cross-tenant leakage prevention (mandatory):**

- Every read/write path: `WHERE tenant_id = :tenantId` with `tenantId` from `TenantContext` only (never from request body for scope).
- Controllers: never accept `tenantId` in body for scope; optional in body only for SuperAdmin cross-tenant ops with separate authorization.
- Repository layer: base class or aspect that injects `tenantId` into all queries.

---

## 1.2 Subscription & Monetization

| Capability | Design | Current State | Gap |
|------------|--------|---------------|-----|
| **Plans** | Free / Pro / Enterprise | ✅ `SubscriptionPlan`, `TenantSubscription` | Map plan IDs to feature flags and quotas in `setting` module |
| **Add-on modules** | e.g. Accommodation as paid add-on | Feature flags in `setting` | Enforce at API gate: check feature flag before accommodation/booking endpoints |
| **Usage-based billing** | API calls, orders, bookings count | Quota enforcer in setting; usage events | Define usage event schema; wire order/booking/payment events to usage aggregator; bill via Stripe metered or similar |
| **Feature flags** | Per-tenant and global | ✅ `FeatureFlagController`, `SettingService`, `QuotaEnforcer` | Document flag keys per vertical; enforce in controllers or aspect |
| **Quotas** | Staff count, inventory SKUs, terminals, API calls | ✅ `QuotaEnforcer` (e.g. catalog) | Extend to all modules: staff count (staff), product count (catalog), terminals (auth POS), etc. |
| **Grace period** | After failed payment, N days before suspend | Not formalized | Add `SubscriptionStatus.GRACE` and job to transition ACTIVE → GRACE → SUSPENDED |
| **Commission model** | Optional % per order/booking | Not present | Optional: configurable % in tenant settings; settlement in `payment`/ledger |

---

## 1.3 Vertical-Specific Logic

### E-commerce

- **Order lifecycle:** ✅ `OrderStateMachine` (AddingItems → … → Completed / Cancelled); confirm, ship, deliver, cancel.
- **Shipping:** ✅ `shippingAddress`, `trackingNumber`; Ship/Deliver handlers.
- **Promotions:** ✅ `promotion` module; voucher application on order.
- **Customer accounts:** ✅ `customer` module; addresses, loyalty; login with account lockout.

### POS

- **Terminal-based sessions:** ✅ `PosTerminal`, `PosSession` (auth); Sale module: `Shift`, `SaleSession` per terminal.
- **Shift management:** ✅ Open/close shift; active shift per staff/terminal.
- **Cash reconciliation:** Report module; end-of-day reconciliation APIs (pending full Z-Out).
- **Receipt issuance:** ✅ `PosReceiptController`, `PosReceiptService` (invoice module).
- **Fast product lookup:** Catalog by SKU/barcode; inventory check.

### Accommodation

- **Room inventory:** ✅ Catalog `ACCOMMODATION` type; booking module for slots.
- **Booking lifecycle:** ✅ `Booking`, `AvailabilityRule`, `BlockedSlot`, `StayPolicy`, `LodgeOperationRule`; SLA monitor and reminder jobs.
- **Seasonal pricing:** Catalog/price rules; booking `PriceCalculatorService`.
- **Check-in / check-out:** Order/booking integration; lodge operation rules (partial).
- **Add-on services:** Order line items; catalog services.
- **Cancellation policy:** Domain in booking; refund handling in payment/order (partial).
- **Deposit handling:** Payment intents; partial capture (Stripe/Bakong).

### Hybrid (Accommodation + POS + E-commerce)

- Single tenant can have all three; feature flags and plan determine which APIs are enabled.
- Shared catalog (products + accommodation); shared inventory for physical goods; booking for rooms; POS for on-site sales (e.g. minibar, restaurant).
- **Conflict resolution:** Inventory: reserve (booking) vs reserve (e-commerce) vs immediate relief (POS); define priority (e.g. POS immediate, then booking, then e-commerce) and document in inventory policy.

---

## 1.4 Financial Integrity

| Requirement | Implementation | Current State | Gap |
|-------------|----------------|---------------|-----|
| **Double-entry ledger** | Every monetary movement as debit/credit entries | ✅ `FinancialLedger` (payment); ledger service | Ensure all payment flows post to ledger; no side-door updates to balance |
| **Payment idempotency** | Idempotency key per payment intent | ✅ `PaymentIntent.idempotencyKey`, `findByIdempotencyKey`; Order confirm/ship/cancel idempotency | Webhook deduplication (WebhookDeduplicationPort) — ensure implemented |
| **Refund handling** | Refund as first-class state; post credit to ledger | Partial (order cancel; payment refund) | Formal refund entity/flow; link to invoice and ledger |
| **Invoice immutability** | Issued invoice is append-only log; no edit after issue | ✅ Invoice versioning; audit; canonical form for signature | Enforce in application: no update of issued invoice content |
| **Tamper-evident audit logs** | Append-only; hash chain or signature | ✅ Invoice Ed25519 signature; audit events | Extend to critical domain events (order state changes, payment state) where required by L2 |

---

## 1.5 Deliverables (Section 1)

### SaaS Business Architecture Spec (summary)

- **Tenancy:** Single DB, shared schema, `tenant_id` + `TenantContext`; lifecycle TRIAL → ACTIVE → SUSPENDED → TERMINATED; GDPR export/deletion workflow to be added.
- **Monetization:** Plans (Free/Pro/Enterprise); add-ons (e.g. Accommodation); usage-based and quotas; feature flags; grace period and optional commission.
- **Verticals:** E-commerce (order, promotion, customer, shipping); POS (shift, session, receipt, reconciliation); Accommodation (booking, seasonal pricing, check-in/out, deposit/cancellation); Hybrid with clear inventory/conflict policy.

### Domain Event Map (high level)

- **Auth:** TenantProvisioned, UserCreated, UserLocked, LoginFailed.
- **Order:** OrderCreated, OrderConfirmed, OrderShipped, OrderCancelled, OrderCompleted.
- **Payment:** PaymentIntentCreated, PaymentCaptured, RefundCreated; LedgerEntryPosted.
- **Invoice:** InvoiceIssued, InvoiceSent (email).
- **Sale:** ShiftOpened, ShiftClosed, SaleSessionCompleted, ReturnRegistered.
- **Inventory:** Reserved, Committed, Released, Adjusted.
- **Promotion:** RedemptionApplied.
- **Booking:** BookingCreated, BookingCancelled, CheckIn, CheckOut.
- **Subscription:** TrialStarted, PlanChanged, SubscriptionSuspended.

### Vertical Capability Matrix

| Capability | E-commerce | POS | Accommodation | Hybrid |
|------------|------------|-----|---------------|--------|
| Order lifecycle | ✅ | ✅ (POS order) | ✅ (booking-linked) | ✅ |
| Payment (card/Bakong) | ✅ | ✅ | ✅ | ✅ |
| Invoice / Receipt | ✅ | ✅ (receipt) | ✅ | ✅ |
| Promotion / Voucher | ✅ | ✅ | ✅ | ✅ |
| Shift / Session | — | ✅ | — | ✅ |
| Booking / Stay | — | — | ✅ | ✅ |
| Inventory (stock) | ✅ | ✅ | By room/night | ✅ |
| Customer / Guest | ✅ | ✅ | ✅ | ✅ |
| Multi-address / Loyalty | ✅ | ✅ | — | ✅ |

### Risk Classification per Module

| Module | Risk | Rationale |
|--------|------|------------|
| auth | Critical | Identity, tenant scope, account lockout |
| payment | Critical | Money, idempotency, ledger, webhooks |
| invoice | Critical | Legal document, signature, immutability |
| order | High | Money, state machine, idempotency |
| sale | High | Money, shift/session, idempotency |
| inventory | High | Stock integrity, reservations |
| promotion | High | Financial (discounts), redemption idempotency |
| customer | Medium | PII, login lockout |
| staff | Medium | RBAC, audit |
| subscription | Medium | Billing, access control |
| catalog | Medium | Data integrity, tenant scope |
| booking | Medium | Financial (deposits), availability |
| report | Low | Read-only aggregations |
| setting | Low | Configuration |
| notification, audit, asset, chat, review | Low | Support; audit is read-heavy |

---

# SECTION 2 — Unified Folder Structure (Mandatory)

## 2.1 Standard (Target)

Every module MUST follow:

```
<module-root>
├── api
├── application
│   ├── command
│   ├── query
│   ├── usecase
│   └── dto
├── domain
│   ├── model
│   ├── event
│   ├── ports      ← repository interfaces HERE only (no domain/repository)
│   ├── policy
│   ├── service
│   └── exception
└── infrastructure
     ├── persistence
     │   └── jpa
     ├── outbox
     ├── saga
     ├── client
     └── config
```

**Rules:**

- Domain has **zero** Spring (or Jakarta persistence) annotations on domain model classes used in core logic; persistence entities may live in infrastructure and be mapped to domain models.
- Repository **interfaces** live only in `domain/ports` (no `domain/repository`).
- Infrastructure implements ports (e.g. `JpaOrderAdapter` implements `OrderRepositoryPort`).
- `package-info.java` in every package.

**Clarification:** Current codebase uses both:

- `domain/ports` (order, payment, invoice, catalog) — **correct**.
- `domain/repository` (sale: `ShiftRepositoryPort`, etc.) — **rename** to `domain/ports` and move interfaces there.
- `domain/port` (invoice, promotion: singular) — **standardize** to `domain/ports` (plural).

---

## 2.2 Module-by-Module Mismatch Analysis

| Module | api | application | domain | infrastructure | Mismatch |
|--------|-----|-------------|--------|----------------|----------|
| auth | ✅ | ✅ command/query | model, event, idp, lock | persistence, security, messaging, aop | Ports not under `domain/ports`; some in idp/lock |
| catalog | ✅ | ✅ command/query/dto | model, **ports** in domain root | persistence | ✅ Mostly aligned; ensure `domain/ports` and package-info everywhere |
| order | ✅ | ✅ command/query/dto | **OrderRepository in domain root**; ports, saga, events | persistence/jpa, messaging, scheduler | Move `OrderRepository`, `OrderOutboxRepository`, `OrderAuditLogRepository` to ports (as interfaces); keep implementation in infra |
| payment | ✅ | ✅ | domain/ports, paymentintent, ledger, webhook | persistence/jpa, stripe, bakong, outbox | ✅ Ports under domain/ports; ensure no Spring in domain entities |
| invoice | ✅ | ✅ | **domain/port** (singular) | persistence, email, pdf, security, outbox | Rename `domain/port` → `domain/ports`; add package-info |
| sale | ✅ | ✅ usecase/query | **domain/repository** (not ports) | persistence/jpa, saga, outbox | Move `domain/repository/*Port` → `domain/ports`; add policy/event/exception where missing |
| staff | ✅ | ✅ | domain (model, enums); repos in? | persistence | Ensure repository interfaces in `domain/ports` |
| customer | ✅ | ✅ | domain, auth (lock policy, session) | persistence, saga, redis | Repository ports in domain/ports |
| inventory | ✅ | ✅ command/query | model, ports | persistence, outbox, saga, cache | Align ports under domain/ports |
| promotion | ✅ | ✅ | domain/model, **domain/port** (PromotionRepository) | persistence/jpa, outbox | Rename port → ports; repository interface in ports |
| subscription | ✅ | ✅ | domain | persistence | Add domain/ports for subscription/plan repos |
| setting | ✅ | ✅ | domain | persistence | Add domain/ports |
| report | ✅ | ✅ | — | — | Report may have no domain entities; optional domain/ports for read models |
| booking | ✅ | ✅ | domain | persistence, jobs | Add domain/ports; ensure no Spring in domain |
| audit | ✅ | — | domain | persistence, listener | Thin domain; ports for audit write |
| notification, asset, chat, review | ✅ | ✅ | domain | various | Standardize to api/application/domain/ports/infrastructure |

---

## 2.3 Refactor Plan (Old → New)

| Change | Action |
|--------|--------|
| **sale: domain/repository → domain/ports** | Move `ShiftRepositoryPort`, `QuotationRepositoryPort`, `SaleSessionRepositoryPort`, `SaleReturnRepositoryPort` to `domain/ports`; update imports; add `package-info.java`. |
| **invoice: domain/port → domain/ports** | Rename package; move `InvoiceRepositoryPort`, `InvoiceSignatureRepositoryPort`, `InvoiceAuditRepositoryPort`, etc. to `domain/ports`. |
| **promotion: domain/port → domain/ports** | Move `PromotionRepository` (interface) to `domain/ports`; rename package. |
| **order: domain repositories → domain/ports** | Replace `OrderRepository`, `OrderOutboxRepository`, `OrderAuditLogRepository` with interfaces in `domain/ports` (e.g. `OrderRepositoryPort`, `OrderOutboxPort`, `OrderAuditPort`); keep JPA impl in infrastructure. (Order already has `OrderRepositoryPort`; remove duplicate `OrderRepository` from domain or make it extend port.) |
| **auth: repository interfaces** | Ensure all repository interfaces used by domain/application live under `domain/ports` (or equivalent); no repository in domain root. |
| **package-info.java** | Add to every package in api, application (command, query, usecase, dto), domain (model, event, ports, policy, service, exception), infrastructure (persistence/jpa, outbox, saga, client, config). |

---

## 2.4 Risk Level per Module (Refactor)

| Module | Refactor Risk | Reason |
|--------|---------------|--------|
| sale | Low | Move 4 interfaces; import updates |
| invoice | Low | Package rename; import updates |
| promotion | Low | One repository interface; adapter impl |
| order | Medium | Multiple repository types; ensure single port per concern |
| auth, customer, staff, subscription, setting, booking | Low–Medium | Add ports package; move interfaces if any in domain root |

---

# SECTION 3 — Cursor Pagination (Strict)

## 3.1 Requirements

- **Offset pagination is forbidden** for list endpoints.
- Sort: `created_at DESC, id DESC`.
- Cursor: Base64-encoded JSON with version, `createdAt`, `id`, and `filterHash`.
- Reject cursor if `filterHash` mismatches (prevents filter tampering).
- Query pattern and index as below.

## 3.2 Cursor Format (Standard)

**Decoded JSON (example):**

```json
{
  "v": 1,
  "createdAt": "2025-03-01T12:00:00Z",
  "id": "12345",
  "filterHash": "sha256_of_filter_params"
}
```

- `v`: schema version (for future evolution).
- `createdAt`, `id`: last item on current page (ISO-8601 and string ID).
- `filterHash`: SHA-256 of canonical filter params (e.g. `tenantId|status|search`) so that cursors from one filter cannot be reused for another.

**Encode:** Base64URL (no padding). Reject if decode fails or `v` unsupported.

## 3.3 Query Pattern

```sql
WHERE tenant_id = :tenantId
  AND (
    created_at < :createdAt
    OR (created_at = :createdAt AND id < :id)
  )
ORDER BY created_at DESC, id DESC
LIMIT :limit + 1
```

- Fetch `limit+1`; if size > limit, `hasNext = true` and next cursor = last item’s (created_at, id). Use same sort for consistency.

## 3.4 Index (Liquibase)

For every list table:

```xml
<createIndex tableName="<table>" indexName="idx_<table>_keyset">
  <column name="tenant_id"/>
  <column name="created_at" descending="true"/>
  <column name="id" descending="true"/>
</createIndex>
```

**Current state:** Sale tables already have keyset indexes (v25). Add equivalent for: order, invoice, promotion (by created_at+id), inventory, customer, staff, booking, payment intents, subscription list.

## 3.5 Response Template

```json
{
  "data": [ ... ],
  "nextCursor": "<base64>",
  "hasNext": true,
  "limit": 20
}
```

Use single DTO: e.g. `CursorPageResponse<T>` with `data`, `nextCursor`, `hasNext`, `limit` (or reuse `CursorPage<T>` and add `limit` in response).

## 3.6 Current Gaps

| Area | Current | Target |
|------|---------|--------|
| **Cursor payload** | Sale: raw `createdAt:id` (Instant.toString); Promotion: `lastId` only; Invoice: `issuedAt:id`; Order/Inventory: encoder with created_at+id | Standardize to JSON with `v`, `createdAt`, `id`, `filterHash` |
| **filterHash** | Not implemented | Compute hash of filter params; store in cursor; reject on mismatch |
| **Common codec** | Per-module encoders (OrderCursorEncoder, CatalogCursorEncoder, etc.) | Single `CursorCodec` in common: encode/decode + hash verification |
| **Page response** | `CursorPage<T>`, `CursorPageResponse<T>`, `OrderCursorResponse`, etc. | One `CursorPageResponse<T>` in common with `data`, `nextCursor`, `hasNext`, `limit` |
| **Index** | Sale (v25); order/invoice/promotion/inventory/customer/staff/booking need keyset index | Add Liquibase changeSets for each list table |

## 3.7 CursorCodec Template (to implement in iecommerce-common)

```java
// Pseudo-signature
public final class CursorCodec {
  public static String encode(int version, Instant createdAt, Object id, String filterHash);
  public static CursorPayload decode(String cursor); // throws if invalid or hash mismatch
  public static String computeFilterHash(String tenantId, Map<String, String> filterParams);
}
```

## 3.8 UAT (Cursor)

- **Concurrency:** Two clients paginate same list; no duplicate items, no gaps when new items inserted.
- **Tenant safety:** Cursor from tenant A must not return tenant B data (reject or treat as no next page).
- **Filter change:** Cursor from filter F1 rejected when used with filter F2 (filterHash mismatch).
- **Stability:** Same cursor + same filters always return same next page (deterministic sort).

---

# SECTION 4 — Security Hardening (OWASP ASVS)

## 4.1 ASVS Level 1 (All Modules)

- Input validation (bean validation, bounds, type).
- Output encoding (no raw user input in HTML/JS).
- Authentication (JWT; no default creds).
- Session (stateless JWT; token expiry).
- Access control (tenant + role; deny by default).
- Cryptography (TLS; no custom crypto; strong algorithms).
- Error handling (no stack traces to client; generic messages).
- Data protection (sensitive data at rest; mask in logs — `@Masked`).
- Communication (HTTPS only).
- Malicious code (dependencies; no eval of user input).
- Business logic (no bypass of workflows via parameter tampering).

## 4.2 ASVS Level 2 (auth, payment, invoice, promotion, refund, staff)

- Stricter authentication (MFA ready; password policy).
- Account lockout: ✅ Auth and customer login lockout; document thresholds.
- Idempotency: ✅ Payment intent, order confirm/ship/cancel, sale quotation, invoice create; extend to refund and other critical writes.
- Rate limiting: ✅ `RateLimitingFilter` in common; apply to auth and payment endpoints.
- Audit logging: ✅ Audit module; AuthEventLogger; staff/customer/order/invoice events; ensure sensitive actions logged (who, what, when, tenant).
- Invoice digital signature: ✅ Ed25519, canonicalizer; verification API.
- Secure key rotation: Key storage (e.g. Vault); rotation procedure for signing keys and API keys.
- Webhook replay protection: Verify signature; store webhook event id (WebhookDeduplicationPort); reject duplicate.

## 4.3 Security Mapping Table per Module

| Module | ASVS L1 | ASVS L2 | Idempotency | Rate Limit | Audit | Notes |
|--------|---------|---------|-------------|------------|-------|-------|
| auth | ✅ | ✅ | N/A (login has lockout) | ✅ | ✅ | Unlock user; tenant provision |
| payment | ✅ | ✅ | ✅ Intent, webhook | ✅ | ✅ | Ledger; webhook dedup |
| invoice | ✅ | ✅ | ✅ Create, email | — | ✅ | Signature verification |
| order | ✅ | — | ✅ Confirm, ship, cancel | — | ✅ | |
| sale | ✅ | — | ✅ Quotation, return | — | ✅ | |
| promotion | ✅ | ✅ | ✅ Redemption key | — | ✅ | |
| staff | ✅ | ✅ | — | — | ✅ | |
| customer | ✅ | — | — | — | ✅ | Login lockout |
| inventory | ✅ | — | ✅ (reservation) | — | ✅ | |
| catalog | ✅ | — | — | — | ✅ | |
| booking | ✅ | — | TBD | — | ✅ | |
| subscription | ✅ | — | — | — | ✅ | |
| report, setting, notification, audit, asset, chat, review | L1 | — | As needed | — | As needed | |

## 4.4 Attack Surface & Mitigation

| Threat | Mitigation |
|--------|------------|
| IDOR (access other tenant’s resource) | Every resource load: verify `resource.tenantId == TenantContext.getCurrentTenant()`; never trust resource ID alone |
| Cursor tampering | filterHash in cursor; reject mismatch |
| Replay (payment webhook) | WebhookDeduplicationPort; idempotency key or event-id store |
| Brute force login | Account lockout; rate limit on login endpoint |
| Privilege escalation | RBAC from Keycloak; staff role checks; SuperAdmin only for cross-tenant ops |
| Invoice forgery | Digital signature; verification API; immutable after issue |
| Refund abuse | Idempotency; business rules (max refund amount, state checks) |

---

# SECTION 5 — Bank-Grade Reliability

## 5.1 State Machine

- **Order:** ✅ `OrderStateMachine`; transitions via domain methods; optimistic locking.
- **Payment:** Payment intent lifecycle (created → authorized → captured / failed); document state machine if not explicit.
- **Booking:** Booking status (e.g. reserved → confirmed → checked-in → checked-out / cancelled); formalize in domain.

## 5.2 Strategy Pattern

- **Payment providers:** Stripe / Bakong adapters implement a common port; add new provider without changing core flow.
- **Promotion rules:** Rule engine / evaluators; strategy for different discount types.

## 5.3 Specification Pattern

- Use for dynamic discount rules (e.g. “order total > X and category = Y”) to keep rules composable and testable.

## 5.4 Outbox Pattern

- ✅ Per-module outbox (order, sale, invoice, promotion, payment, inventory, customer); relay scheduler; at-least-once publish.
- Ensure one outbox table per bounded context; relay retries with backoff; idempotent consumers where applicable.

## 5.5 Saga

- **Sale:** ✅ `SaleSagaOrchestrator`, `SaleSagaListener`, state entity; compensation on failure.
- **Order:** ✅ `OrderSagaState`, steps, listener.
- **Auth:** ✅ Tenant provisioning saga.
- **Customer / Inventory:** Event-driven; compensate via compensating commands/events.

## 5.6 Idempotency Store

- ✅ Payment intent, order confirm/ship/cancel, sale quotation, invoice create/email; key = client-provided idempotency key or composite (e.g. invoiceId:messageType).
- Store: response snapshot + key; TTL or retention per policy.

## 5.7 Retry and Circuit Breaker

- Outbox relay: retry with exponential backoff + jitter; max attempts then dead-letter or alert.
- External calls (Stripe, email, PDF): retry with backoff; circuit breaker (e.g. Resilience4j) for external provider failures.

## 5.8 Async vs Sync Boundaries

- **Sync:** API request → command/query → domain → repository → DB; return response.
- **Async:** Outbox relay → message broker → saga listener / event handler; eventual consistency; do not expose “eventually consistent” data as immediately consistent in same request.

## 5.9 Transaction Boundaries

- One transaction per command (write); include outbox insert in same transaction; read-your-writes within same request.
- Saga steps: one transaction per step; outbox or saga state updated in same transaction.

## 5.10 Compensation Logic

- Document for each saga: on step N failure, which compensating actions (e.g. release reservation, cancel payment intent); ensure compensation is idempotent.

---

# SECTION 6 — Database Strategy

## 6.1 Index Strategy

- **Primary keys:** All tables have PK (id).
- **Tenant + keyset:** `(tenant_id, created_at DESC, id DESC)` for every list endpoint (see Section 3).
- **Lookups:** Unique on (tenant_id, business_key) where applicable (e.g. order code, invoice number, slug).
- **Foreign keys:** Enforce referential integrity; index FK columns used in joins.

## 6.2 Unique Constraints

- Idempotency: `(tenant_id, idempotency_key)` or `(idempotency_key)` where global.
- Business keys: e.g. (tenant_id, code) for order; (tenant_id, number) for invoice; (tenant_id, slug) for product.

## 6.3 Liquibase Discipline

- Single master changelog; include module-specific changelogs by version.
- One changeSet per logical change; idempotent (use `preConditions` if needed).
- No destructive changes without migration path (e.g. rename column with copy + drop).
- Cursor indexes in dedicated changeSet (e.g. v25-style).

## 6.4 Archiving Strategy

- Hot data in main tables; archive old orders/invoices/audit to archive tables or cold storage by date; retain for compliance (e.g. 7 years); document retention per entity.

## 6.5 Soft Delete vs Hard Delete

- **Soft delete:** Tenant, User (block login; retain for audit); optional for Customer.
- **Hard delete:** Only where required (e.g. GDPR purge); else anonymize + soft delete.
- **Audit / events:** Append-only; no delete (or legal hold only).

## 6.6 Partitioning

- Consider by `tenant_id` or by time (e.g. `created_at` month) for very large tables (orders, audit_events); PostgreSQL partitioning; evaluate when row counts justify.

## 6.7 Postgres vs MySQL

- **Types:** JSON/JSONB (Postgres) vs JSON (MySQL); use consistently.
- **Sequences:** Postgres SERIAL/BIGSERIAL vs MySQL AUTO_INCREMENT; Liquibase abstracts.
- **Concurrency:** Optimistic locking (`@Version`) works on both; row-level locking (SELECT FOR UPDATE) syntax differs slightly.
- **Indexes:** Descending indexes (keyset) supported both; check syntax in Liquibase for MySQL if needed.

## 6.8 Concurrency Conflict Handling

- Optimistic locking: `@Version` on Order, Invoice, Payment, Sale entities, etc.; return 409 or retry on `OptimisticLockException`.
- Unique constraint violations: return 409 or 4xx with clear message (e.g. duplicate idempotency key).

---

# SECTION 7 — Performance Plan

## 7.1 N+1 Prevention

- Use JOIN FETCH or entity graph for one-to-many when loading aggregate (e.g. order + items); avoid lazy load in loops.
- DTO projections: select only needed columns in query handlers; avoid loading full entity when only list view needed.

## 7.2 Projection Usage

- List endpoints: projection (id, created_at, status, summary fields) instead of full entity.
- Detail endpoint: load full aggregate when needed.

## 7.3 Caching Strategy

- **Catalog:** ✅ Redis cache adapter; invalidate on write.
- **Tenant/plan/feature flags:** Cache in memory with TTL; invalidate on update.
- **Sensitive data:** Do not cache payment details or full PII in shared cache; cache only by tenant and short TTL if needed.
- **Cursor pages:** Do not cache cursor responses (stale nextCursor); cache only static or slowly changing data.

## 7.4 Load Testing Plan

- Target: list endpoints (cursor) at 100–500 req/s per tenant; order submit, payment capture at defined throughput.
- Tools: Gatling or k6; scenarios: login → list orders (cursor) → get order → create order (idempotency key).
- Measure: p95/p99 latency; error rate; DB connection pool; outbox relay lag.

## 7.5 Concurrency Stress Tests

- Multiple threads paginating same list; assert no duplicates/gaps.
- Concurrent order confirm with same idempotency key: one success, rest get cached response.
- Optimistic lock: concurrent update to same order; one wins, others get 409 or retry.

## 7.6 Memory Analysis

- Profile heap for large list queries; avoid loading unbounded result sets; use limit+cursor only.
- Check for cache growth (e.g. per-tenant caches without eviction).

---

# SECTION 8 — Multi-Vertical UAT Plan

## 8.1 E-commerce UAT

- **Flow:** Create order → Confirm (idempotency) → Payment capture → Invoice issued → (optional) Refund partial.
- **Acceptance:** Order state transitions; payment and invoice linked; idempotent confirm; cursor list orders (no duplicates/gaps).
- **Security:** IDOR: try to access another tenant’s order (must 404/403). Cursor from tenant A not valid for tenant B.
- **Concurrency:** Two confirm requests with same idempotency key → same response.

## 8.2 POS UAT

- **Flow:** Open shift → Create sale session → Add items → Complete session → Close shift → Reconcile (Z-Out).
- **Acceptance:** Shift open/close; session linked to shift; receipt generated; inventory relieved; cursor list shifts/sessions.
- **Security:** Only staff with role can open shift; tenant isolation.
- **Concurrency:** Two cashiers, two shifts; no cross-tenant data.

## 8.3 Accommodation UAT

- **Flow:** Create booking → Modify dates → Check-in → Add services → Check-out → Invoice → Partial refund (e.g. minibar).
- **Acceptance:** Booking lifecycle; availability updated; invoice after checkout; refund posts to ledger.
- **Security:** Guest cannot see other guests’ bookings; tenant isolation.
- **Concurrency:** Double-booking attempt (same room, same night) → one succeeds, one fails or wait-list.

## 8.4 Hybrid UAT

- **Scenario:** Tenant has accommodation + POS. Active booking; POS sale (e.g. minibar) during stay.
- **Acceptance:** POS order can attach to booking; inventory and ledger correct; no conflict between booking and POS for same tenant.
- **Conflict:** Define and test inventory conflict resolution (e.g. room minibar stock vs retail stock).

## 8.5 Cursor Pagination Correctness Tests

- Paginate full list; collect all IDs; assert no duplicates, no missing IDs (vs single query count).
- Insert new row while paginating; cursor stability (no skip or duplicate).
- filterHash: change filter, reuse old cursor → reject or empty.

---

# SECTION 9 — Product Readiness Checklist

- [ ] **Multi-tenant isolation:** All queries and APIs scoped by tenant; IDOR tests pass.
- [ ] **Billing logic:** Subscription plans and quotas enforced; grace period and suspend flow implemented.
- [ ] **Feature toggles:** Per-tenant and per-plan flags; enforced on vertical APIs (e.g. accommodation, POS).
- [ ] **Invoice signature verification:** API to verify signature; documented for auditors.
- [ ] **Payment idempotency:** Intent and webhook dedup; no double charge.
- [ ] **Audit logs integrity:** Critical actions logged; log retention and access control.
- [ ] **Cursor pagination under load:** No offset usage; indexes in place; UAT passed.
- [ ] **UAT passed:** E-commerce, POS, Accommodation, Hybrid scenarios green.
- [ ] **Rollback plan tested:** DB rollback (Liquibase rollback or forward-only with fix); deployment rollback procedure.

---

# SECTION 10 — Long-Term Roadmap

## 10.1 One-Year Product Roadmap

- **Q1:** SaaS foundation hardening: tenant lifecycle (suspend/terminate), GDPR export/deletion, cursor standard (CursorCodec + filterHash), unified folder structure (ports), ASVS L2 coverage.
- **Q2:** Vertical completeness: accommodation (cancellation, deposit, check-in/out), POS (Z-Out, thermal receipt), refund flow and ledger.
- **Q3:** Usage-based billing, feature flags per plan, quota enforcement across all modules, SuperAdmin control plane (if not done).
- **Q4:** Public API (developer portal), rate limits per plan, API keys, documentation; first paying tenants.

## 10.2 Enterprise Scaling Roadmap

- Read replicas for report and list queries; write to primary.
- Caching (tenant/plan, catalog) with invalidation.
- Partitioning for orders, audit_events by time or tenant.
- Multi-region: tenant affinity (data residency); replicate critical data per region.

## 10.3 Microservice Migration Strategy

- Modular monolith first; clear bounded contexts (order, payment, invoice, sale, booking, etc.).
- Extract by domain: e.g. Payment as first candidate (Stripe/Bakong, webhooks); then Invoice (PDF, email); then Order (state machine, saga).
- Communication: events (Kafka) + outbox; sync APIs only where necessary (e.g. inventory reserve from order).
- Database: per-service DB when extracting; shared DB temporarily with schema ownership boundaries.

## 10.4 Public API Monetization

- API keys per tenant (or per app); rate limits by plan; usage metering (calls, orders, bookings).
- Developer portal: docs, sandbox, key management.
- Billing: Stripe metered or similar for overage.

## 10.5 AI / Demand Forecasting

- Use case: demand prediction for inventory and pricing (seasonal, promotions).
- Data: historical orders, bookings, promotions; aggregate per product/category/tenant.
- Deploy as separate service or internal API; PII and tenant isolation preserved.

## 10.6 Multi-Region Deployment

- Active-passive or active-active per region; tenant data residency; RTO/RPO defined.
- Secrets (Vault) and config per region; Keycloak replication or federated.

---

# OUTPUT SUMMARY

## 1) SaaS Business Architecture Spec

- Multi-tenant shared schema; lifecycle TRIAL → ACTIVE → SUSPENDED → TERMINATED; GDPR export/deletion to add.
- Subscription plans (Free/Pro/Enterprise); add-ons; usage-based and quotas; feature flags; grace period.
- Verticals: E-commerce, POS, Accommodation, Hybrid; financial integrity (ledger, idempotency, immutability, audit).

## 2) Vertical Capability Matrix

- See table in Section 1.5 (E-commerce, POS, Accommodation, Hybrid rows).

## 3) Unified Structure Standard

- api / application (command, query, usecase, dto) / domain (model, event, ports, policy, service, exception) / infrastructure (persistence/jpa, outbox, saga, client, config); repository interfaces only in domain/ports.

## 4) Module-by-Module Gap Analysis

- See Section 2.2 (per-module mismatch) and Section 2.3 (refactor plan).

## 5) Cursor Pagination Design

- Sort: created_at DESC, id DESC. Cursor: Base64 JSON with v, createdAt, id, filterHash; reject on hash mismatch. Query and index pattern in Section 3. Common CursorCodec and CursorPageResponse; Liquibase indexes for all list tables.

## 6) Security Mapping Table

- See Section 4.3 (per-module ASVS L1/L2, idempotency, rate limit, audit).

## 7) Database Strategy

- Index (keyset, unique, FK); Liquibase discipline; archiving; soft vs hard delete; partitioning when needed; Postgres/MySQL notes; optimistic locking.

## 8) Performance Plan

- N+1 prevention; projections; safe caching; load and concurrency tests; memory awareness.

## 9) UAT Plan

- E-commerce, POS, Accommodation, Hybrid flows; acceptance criteria; security and cursor correctness tests (Section 8).

## 10) Product Readiness Checklist

- Section 9 (multi-tenant, billing, feature toggles, invoice signature, payment idempotency, audit, cursor, UAT, rollback).

## 11) Risk Analysis

- Module risk classification (Section 1.5); refactor risk (Section 2.4); security threats and mitigations (Section 4.4).

## 12) Implementation Roadmap

- Section 10.1 (one-year); Section 2.3 (refactor order); cursor standard (Section 3); ASVS L2 (Section 4).

## 13) Definition of Done Checklist

- Story/feature level:
  - Code follows unified folder structure; domain has no Spring; repositories in domain/ports.
  - List endpoints use cursor only; CursorCodec + filterHash when implemented; index present.
  - Security: input validation; tenant + IDOR check; idempotency where required; audit log for sensitive actions.
  - Tests: unit (domain, application); integration (API + DB); UAT scenario if user-facing flow.
  - Docs: JavaDoc for public APIs; changelog entry for DB or API change.
  - No new critical/high linter or security findings.

---

*End of SaaS Enterprise Architecture Spec.*
