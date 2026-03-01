# Module Completeness Matrix

**Version:** 1.0  
**Source:** Full repo scan + SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md  
**Date:** 2025-03-01

This matrix scores each module on API completeness, business rules, integrations, tenant isolation, security, observability, performance, data integrity, and tests. Gaps are classified by severity, type, and fix strategy.

---

## Modules in Scope

| # | Module | Risk (Spec) | Primary vertical |
|---|--------|-------------|------------------|
| 1 | auth | Critical | All |
| 2 | customer | Medium | E-commerce, POS, Accommodation |
| 3 | staff | Medium | All |
| 4 | catalog | Medium | All |
| 5 | inventory | High | All |
| 6 | order | High | E-commerce, POS |
| 7 | promotion | High | E-commerce, POS, Accommodation |
| 8 | payment | Critical | All |
| 9 | invoice | Critical | All |
| 10 | sale | High | POS |
| 11 | subscription | Medium | All |
| 12 | setting | Low | All |
| 13 | report | Low | All |
| 14 | booking | Medium | Accommodation |
| 15 | audit | Low | All |
| 16 | notification | Low | All |
| 17 | asset | Low | All |
| 18 | chat | Low | All |
| 19 | review | Low | E-commerce |
| 20 | iecommerce-common | — | Shared |
| 21 | iecommerce-app | — | Bootstrap |

---

## 1. auth

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 8/10 | User CRUD, tenant CRUD, OTP, lockout; list users may use offset (UserQueryHandler PageRequest). |
| Business rules | 8/10 | Tenant lifecycle (TRIAL/ACTIVE/GRACE/SUSPENDED/TERMINATED) enforced in TenantContextFilter; lockout policy exists. |
| Cross-module | 8/10 | Tenant provisioning saga; Keycloak client; outbox/messaging. |
| Tenant isolation | 9/10 | Tenant from JWT only; TenantContextFilter blocks by status. |
| Security (ASVS) | 8/10 | L2: rate limiting, lockout, audit; MFA ready. |
| Observability | 6/10 | Logging present; correlationId/MDC and structured tenantId/requestId not standardized. |
| Performance | 7/10 | User list may use offset; keyset index for user list if paginated. |
| Data integrity | 8/10 | Tenant, User; optimistic locking where needed. |
| Tests | 6/10 | Some unit/integration; missing tenant status filter tests, GRACE read-only tests. |

**Gap list (auth)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| User list offset pagination | Medium | Performance | Migrate to cursor; keyset (tenant_id, created_at, id). |
| correlationId/requestId in logs | Low | Architecture | Add MDC filter + logging helper in common. |
| Tenant status + GRACE tests | High | Security | Add integration tests for SUSPENDED/TERMINATED/GRACE. |

---

## 2. customer

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 7/10 | CRUD, list; search (name/email/phone) may be partial. |
| Business rules | 7/10 | Login lockout; address/loyalty; no formal state machine. |
| Cross-module | 7/10 | Auth lock port; events; outbox. |
| Tenant isolation | 8/10 | Controllers use TenantContext; repository must scope by tenantId. |
| Security | 7/10 | L1; lockout in auth/customer. |
| Observability | 6/10 | Same as auth. |
| Performance | 6/10 | List endpoint: cursor vs offset unclear; keyset index. |
| Data integrity | 7/10 | Unique constraints; idempotency on critical writes not explicit. |
| Tests | 5/10 | Gaps in list pagination, tenant isolation, search. |

**Gap list (customer)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| domain/auth/port (singular) | Medium | Architecture | Rename to domain/ports or move to ports. |
| Customer list cursor + keyset | High | Performance | Cursor pagination; keyset index. |
| Search by name/email/phone | Medium | Functional | Safe search pattern; cursor; indexes. |

---

## 3. staff

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 7/10 | CRUD, list; RBAC; list uses keyset (findFirstPage/findNextPage). |
| Business rules | 7/10 | Role-based; no formal state machine. |
| Cross-module | 6/10 | Auth integration; audit. |
| Tenant isolation | 8/10 | Tenant-scoped repos. |
| Security | 8/10 | L2; audit; RBAC. |
| Observability | 6/10 | Standard. |
| Performance | 8/10 | Cursor-style list (PageRequest size only). |
| Data integrity | 7/10 | Unique staff per tenant. |
| Tests | 5/10 | More integration tests for list + tenant isolation. |

**Gap list (staff)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| Standardize list to CursorCodec/FilterHasher | Medium | Architecture | Use common cursor types; filterHash if filters exist. |
| package-info coverage | Low | Architecture | Add missing package-info. |

---

## 4. catalog

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 8/10 | Product/category CRUD, list; search by SKU/barcode/name. |
| Business rules | 7/10 | Product types (e.g. ACCOMMODATION); validations. |
| Cross-module | 7/10 | Inventory, order, booking reference catalog. |
| Tenant isolation | 8/10 | tenant_id scoping. |
| Security | 7/10 | L1; quota enforcer. |
| Observability | 6/10 | Standard. |
| Performance | 7/10 | ProductRepositoryAdapter uses PageRequest.of(0, limit); CategoryRepositoryAdapter keyset. Cursor standard + keyset index. |
| Data integrity | 8/10 | Unique (tenant_id, slug/code); constraints. |
| Tests | 6/10 | Some tests; search and cursor contract tests. |

**Gap list (catalog)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| Product list: full CursorCodec + filterHash | Medium | Performance/Architecture | Align with common pagination. |
| Search: safe patterns + length limit | High | Security | Document and enforce; no leading wildcard abuse. |

---

## 5. inventory

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 7/10 | Ledger, reservation, adjustment; list endpoints. |
| Business rules | 8/10 | Reserve/commit/release; saga; conflict policy (reservation vs POS vs eCommerce) not formalized. |
| Cross-module | 8/10 | Order, sale, booking; outbox; saga. |
| Tenant isolation | 8/10 | tenant_id in queries. |
| Security | 7/10 | L1; idempotency on reservation. |
| Observability | 6/10 | Standard. |
| Performance | 6/10 | JpaLedgerAdapter, JpaReservationAdapter use PageRequest.of(0, limit); keyset indexes. |
| Data integrity | 8/10 | Ledger entries; reservation unique; InventoryOutboxRepository in domain (move to ports). |
| Tests | 5/10 | Reservation idempotency; conflict policy; tenant isolation. |

**Gap list (inventory)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| InventoryOutboxRepository in domain root | Medium | Architecture | Move to domain/ports; adapter implements port. |
| Ledger/reservation list: cursor standard | Medium | Performance | CursorCodec; keyset index. |
| Conflict policy (reservation vs POS vs eCommerce) | High | Functional | Document and implement deterministic rules. |

---

## 6. order

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 9/10 | Full CRUD, list (keyset), audit list, state transitions. |
| Business rules | 9/10 | OrderStateMachine; idempotency confirm/ship/cancel; saga. |
| Cross-module | 9/10 | Outbox; saga; payment, invoice, inventory ports. |
| Tenant isolation | 9/10 | TenantContext; findByTenantIdAndId; TenantGuard applicable. |
| Security | 8/10 | L1; idempotency; audit. |
| Observability | 6/10 | Standard. |
| Performance | 8/10 | List uses keyset (findFirstPage/findNextPage); PageRequest for limit only. |
| Data integrity | 9/10 | @Version; outbox; idempotency. |
| Tests | 6/10 | More integration: list cursor, tenant isolation, concurrency. |

**Gap list (order)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| Order list: CursorCodec + FilterHasher | Medium | Architecture | Align with common; filterHash for state filter. |
| package-info in all packages | Low | Architecture | Add missing. |

---

## 7. promotion

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 8/10 | CRUD, list (cursor with lastId); redemption. |
| Business rules | 7/10 | Rules engine; stacking not fully formalized; redemption idempotency. |
| Cross-module | 7/10 | Order, sale; outbox. |
| Tenant isolation | 8/10 | tenant_id scoping. |
| Security | 8/10 | L2; redemption idempotency; unique constraint on redemption key. |
| Observability | 6/10 | Standard. |
| Performance | 7/10 | PromotionPersistenceAdapter uses lastId + PageRequest; align with CursorCodec. |
| Data integrity | 7/10 | Redemption unique (tenant + promotion + customer/order); outbox. |
| Tests | 5/10 | Stacking rules; duplicate redemption; filterHash. |

**Gap list (promotion)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| List: CursorCodec + filterHash | Medium | Architecture | Replace lastId cursor with common format. |
| Redemption idempotency + unique constraint | High | Data/Security | DB unique + test. |
| Stacking rules: Strategy + Specification | Medium | Functional | Document and implement composable rules. |

---

## 8. payment

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 8/10 | Intent create/capture; list (tenant-scoped); webhook. |
| Business rules | 8/10 | Intent lifecycle; ledger; idempotency key. |
| Cross-module | 9/10 | Stripe/Bakong/PayPal; ledger; webhook dedup. |
| Tenant isolation | 8/10 | tenant_id on intents; verify every load. |
| Security | 8/10 | L2; webhook signature + replay; idempotency. |
| Observability | 6/10 | Standard. |
| Performance | 6/10 | JpaPaymentIntentAdapter list uses PageRequest.of(0, limit); keyset index. |
| Data integrity | 8/10 | Ledger double-entry; idempotency; webhook dedup. |
| Tests | 5/10 | Webhook duplicate rejected; invalid signature; idempotency key same response. |

**Gap list (payment)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| PaymentRepository, PaymentOutboxRepository, etc. in domain root | High | Architecture | Move all to domain/ports; single naming (Port). |
| Intent list: CursorCodec + keyset index | Medium | Performance | Standard cursor; filterHash if filters. |
| Webhook verification + replay tests | High | Security | Strict signature; replay dedup test. |

---

## 9. invoice

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 8/10 | CRUD, list (issuedAt + id cursor); issue, send email, verify. |
| Business rules | 8/10 | Immutable after issue; signature; audit. |
| Cross-module | 8/10 | Order, payment; PDF; email. |
| Tenant isolation | 9/10 | tenant_id; domain/ports. |
| Security | 8/10 | L2; signature verification; immutability. |
| Observability | 6/10 | Standard. |
| Performance | 7/10 | JpaInvoiceRepositoryAdapter, JpaInvoiceAuditAdapter use PageRequest; keyset. |
| Data integrity | 8/10 | Signature; no update after issue. |
| Tests | 5/10 | Tampered invoice fails verification; issued invoice not editable. |

**Gap list (invoice)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| List/audit list: CursorCodec + filterHash | Medium | Architecture | Common cursor. |
| Verification endpoint + tests | High | Security | GET verify; test tamper + immutability. |

---

## 10. sale

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 9/10 | Shifts, sessions, quotations, returns; list with CursorCodec/FilterHasher. |
| Business rules | 9/10 | Shift open/close; session; idempotency quotation/return; saga. |
| Cross-module | 9/10 | Order, inventory, payment; outbox; saga. |
| Tenant isolation | 9/10 | TenantContext; domain/ports. |
| Security | 8/10 | L1; idempotency. |
| Observability | 6/10 | Standard. |
| Performance | 9/10 | Keyset; CursorPageResponse; filterHash. |
| Data integrity | 8/10 | Optimistic locking; idempotency. |
| Tests | 5/10 | Integration tests for list (ordering, no duplicates, filter mismatch, tenant isolation). |

**Gap list (sale)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| Sale list integration tests | High | Functional | Add tests per spec Phase 2.6. |

---

## 11. subscription

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 7/10 | Plan, tenant subscription; list. |
| Business rules | 7/10 | TRIAL/ACTIVE/GRACE/SUSPENDED; link to tenant status. |
| Cross-module | 7/10 | Auth tenant status; payment billing. |
| Tenant isolation | 8/10 | tenant_id. |
| Security | 7/10 | L1. |
| Observability | 6/10 | Standard. |
| Performance | 6/10 | List cursor/keyset if present. |
| Data integrity | 7/10 | Plan constraints. |
| Tests | 5/10 | Status transitions; grace period. |

**Gap list (subscription)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| domain/ports for plan and subscription repos | Medium | Architecture | Add ports; implement in infra. |
| List cursor + keyset | Medium | Performance | If list exists. |

---

## 12. setting

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 8/10 | Global/tenant settings; feature flags; quotas. |
| Business rules | 7/10 | isFeatureEnabled; getQuota; vertical/capability not formalized. |
| Cross-module | 8/10 | Used by all; QuotaEnforcer. |
| Tenant isolation | 8/10 | Tenant-scoped settings. |
| Security | 7/10 | L1; no PII in settings. |
| Observability | 6/10 | Standard. |
| Performance | 7/10 | Cached where needed. |
| Data integrity | 7/10 | Unique (tenant_id, key). |
| Tests | 5/10 | Feature flag and quota enforcement tests. |

**Gap list (setting)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| Tenant Capability Model (verticalMode, enabledModules) | High | Functional | Add capability model; gate service; docs. |

---

## 13. report

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 6/10 | Aggregations; may have list endpoints. |
| Business rules | 6/10 | Read-only. |
| Cross-module | 6/10 | Reads from order, sale, etc. |
| Tenant isolation | 8/10 | Must be tenant-scoped. |
| Security | 7/10 | L1. |
| Observability | 6/10 | Standard. |
| Performance | 6/10 | Heavy queries; cursor if list. |
| Data integrity | N/A | Read-only. |
| Tests | 4/10 | Report contract tests. |

**Gap list (report)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| List endpoints cursor | Medium | Performance | If any list. |
| package-info | Low | Architecture | Add. |

---

## 14. booking

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 7/10 | Booking CRUD, availability, check-in/out. |
| Business rules | 7/10 | AvailabilityRule, StayPolicy, LodgeOperationRule; deposit. |
| Cross-module | 7/10 | Order, payment, inventory. |
| Tenant isolation | 8/10 | tenant_id. |
| Security | 7/10 | L1; idempotency on booking create. |
| Observability | 6/10 | Standard. |
| Performance | 6/10 | List/search cursor + keyset. |
| Data integrity | 7/10 | Unique constraints; optimistic locking. |
| Tests | 5/10 | Availability, check-in/out, deposit flow. |

**Gap list (booking)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| domain/ports | Medium | Architecture | Repository interfaces in ports. |
| Search availability + booking list cursor | Medium | Performance | Keyset; CursorCodec. |

---

## 15. audit

| Dimension | Score | Gaps |
|-----------|-------|------|
| API completeness | 6/10 | Query/export; list. |
| Business rules | 6/10 | Append-only; event types. |
| Cross-module | 7/10 | Listeners from order, auth, etc. |
| Tenant isolation | 8/10 | tenant_id. |
| Security | 7/10 | L1; no deletion. |
| Observability | 7/10 | Audit is observability. |
| Performance | 6/10 | List cursor; retention/archiving. |
| Data integrity | 8/10 | Append-only. |
| Tests | 4/10 | Audit write + query tests. |

**Gap list (audit)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| List cursor + keyset | Medium | Performance | If list endpoint. |

---

## 16–19. notification, asset, chat, review

| Dimension | Score | Gaps (summary) |
|-----------|-------|----------------|
| API completeness | 6–7/10 | CRUD/list per module; may use offset. |
| Business rules | 5–7/10 | Module-specific. |
| Cross-module | 5–6/10 | Events/clients. |
| Tenant isolation | 7–8/10 | Must enforce tenant_id. |
| Security | 6–7/10 | L1. |
| Observability | 6/10 | Standard. |
| Performance | 5–6/10 | Cursor where list exists. |
| Data integrity | 6–7/10 | Constraints. |
| Tests | 4–5/10 | Add critical path tests. |

**Cross-cutting gaps (16–19)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| domain/ports consistency | Medium | Architecture | No domain/repository or domain/port. |
| List cursor | Medium | Performance | CursorCodec where lists exist. |
| package-info | Low | Architecture | Every package. |

---

## 20. iecommerce-common

| Dimension | Score | Gaps |
|-----------|-------|------|
| Pagination | 9/10 | CursorPayload, CursorCodec, FilterHasher, CursorPageResponse; tests. |
| Security | 8/10 | TenantGuard; TenantContext; exception handler for InvalidCursorException. |
| Observability | 5/10 | Logging helper with correlationId/tenantId/requestId not formalized. |
| Outbox | 8/10 | AbstractOutboxRelay; BaseOutboxEvent. |

**Gap list (common)**

| Gap | Severity | Type | Fix strategy |
|-----|----------|------|--------------|
| Logging helper (MDC correlationId, structured fields) | Medium | Observability | Add helper; document. |
| Contract test helper for cursor lists | High | Tests | Reusable test for cursor list endpoints. |

---

## Summary: Critical/High Gaps by Type

| Type | Critical | High |
|------|----------|------|
| Security | — | Payment webhook verification + replay tests; Invoice verification + immutability tests; Auth tenant status tests |
| Functional | — | Sale list integration tests; Tenant Capability Model; Inventory conflict policy |
| Data | — | Promotion redemption unique constraint + idempotency |
| Performance | — | Multiple modules: cursor standard (CursorCodec) where still using custom cursor |
| Architecture | Payment repos in domain root | Customer domain/port → ports; Inventory outbox in ports |

---

*End of MODULE_COMPLETENESS_MATRIX.md*
