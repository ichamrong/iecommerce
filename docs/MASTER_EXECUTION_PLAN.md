# Master Execution Plan

**Version:** 1.0  
**Source:** MODULE_COMPLETENESS_MATRIX.md + SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md  
**Date:** 2025-03-01

This plan orders tasks by dependency and risk. Execute in phases; each task is sized for a single commit where possible.

---

## Phase 1 — Foundation (no module-specific dependencies)

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 1.1 | Add logging helper in common (MDC correlationId, tenantId, requestId; no PII) | iecommerce-common | Low | None |
| 1.2 | Add contract test helper for cursor list endpoints (reusable assertions) | iecommerce-common | Low | None |
| 1.3 | Create TENANT_CAPABILITY_MODEL.md and TenantCapabilityService + verticalMode/enabledModules in setting | setting, common | Medium | None |
| 1.4 | Add capability gate (aspect or filter) that blocks disabled modules/vertical; stable error code | common, setting | Medium | 1.3 |

---

## Phase 2 — Architecture (domain/ports; no offset)

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 2.1 | Move payment domain repositories to domain/ports (PaymentIntentRepositoryPort, FinancialLedgerPort, WebhookDeduplicationPort, WebhookVerificationPort, outbox port) | payment | High | None |
| 2.2 | Rename customer domain/auth/port → domain/ports (or consolidate auth ports) | customer | Low | None |
| 2.3 | Move InventoryOutboxRepository to domain/ports; adapter implements port | inventory | Low | None |
| 2.4 | Migrate auth user list to cursor pagination (keyset; CursorCodec) | auth | Medium | common pagination |
| 2.5 | Migrate payment intent list to CursorCodec + keyset index | payment | Medium | 2.1 |
| 2.6 | Migrate invoice list + audit list to CursorCodec + filterHash | invoice | Medium | common |
| 2.7 | Migrate order list to CursorCodec + filterHash (state filter) | order | Medium | common |
| 2.8 | Migrate catalog product/category list to CursorCodec where not already | catalog | Low | common |
| 2.9 | Migrate staff list to CursorCodec | staff | Low | common |
| 2.10 | Migrate inventory ledger/reservation list to cursor standard | inventory | Low | common |
| 2.11 | Migrate promotion list to CursorCodec (replace lastId) | promotion | Low | common |
| 2.12 | Add keyset Liquibase indexes for all list tables missing them (order, invoice, payment_intent, catalog, staff, inventory, booking, subscription) | per module | Low | — |

---

## Phase 3 — Security (ASVS L2)

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 3.1 | Tenant status integration tests (ACTIVE ok; SUSPENDED/TERMINATED 403; GRACE GET ok, POST 403) | auth | Medium | None |
| 3.2 | Payment: strict webhook signature verification + replay dedup (unique event id); tests | payment | High | 2.1 |
| 3.3 | Invoice: verification endpoint (GET) + tampered/immutability tests | invoice | High | None |
| 3.4 | Promotion: redemption unique constraint (tenant + promotion + customer/order) + idempotency test | promotion | High | None |
| 3.5 | Apply TenantGuard or equivalent to payment, invoice, booking single-resource loads | payment, invoice, booking | Medium | None |

---

## Phase 4 — Business logic + data integrity

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 4.1 | Document and implement inventory conflict policy (reservation vs POS vs eCommerce; deterministic) | inventory, docs | High | None |
| 4.2 | Promotion stacking: Strategy for discount type; Specification for eligibility; document | promotion | Medium | None |
| 4.3 | Booking: idempotency on create; check-in/check-out state machine | booking | Medium | None |
| 4.4 | Subscription: GRACE transition job (ACTIVE → GRACE → SUSPENDED) | subscription, auth | Medium | None |

---

## Phase 5 — Search + observability

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 5.1 | Document SEARCH_AND_QUERY_STANDARDS.md (catalog, customer, order, sale, booking; safe patterns; cursor) | docs | Low | None |
| 5.2 | Implement search endpoints where missing: catalog (sku/barcode/name), customer (name/email/phone), order (code/status/date), sale (shift/session/date), booking (guest/date/status) | catalog, customer, order, sale, booking | Medium | 5.1, cursor |
| 5.3 | Correlation ID filter + MDC; ensure no PII in logs | common, app | Low | 1.1 |

---

## Phase 6 — Package-info + null-safety + JPA

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 6.1 | Add package-info.java to every package (api, application/*, domain/*, infrastructure/*) | all | Low | None |
| 6.2 | Create JPA_ENTITY_GUIDE.md (@Version, @NonNullFields, Lombok policy) | docs | Low | None |
| 6.3 | Fix @Version and null-safety issues triggered by package-info / NonNullFields | affected modules | Medium | 6.1, 6.2 |

---

## Phase 7 — Tests

| Order | Task | Module(s) | Risk | Dependency |
|-------|------|----------|------|------------|
| 7.1 | Sale: integration tests for list endpoints (ordering, no duplicates, filter mismatch 400, tenant isolation) | sale | High | common contract helper |
| 7.2 | Order + payment + invoice: integration test (e-commerce canonical flow) | order, payment, invoice | High | — |
| 7.3 | Sale POS flow: shift → session → complete → receipt | sale, invoice | Medium | — |
| 7.4 | Booking: availability → create → check-in → check-out (or cancel) | booking | Medium | — |
| 7.5 | Webhook replay + invalid signature tests | payment | High | 3.2 |
| 7.6 | Tenant isolation: tenant A cannot access tenant B resource by ID | sale, order, payment, invoice | High | — |

---

## Phase 8 — Documentation + final validation

| Order | Task | Output | Dependency |
|-------|------|--------|-------------|
| 8.1 | DB_SCHEMA_RULES_AND_INDEXES.md | Index list; unique constraints; retention | — |
| 8.2 | SECURITY_AND_RELIABILITY_HARDENING.md | ASVS mapping; outbox; saga; idempotency; retry | — |
| 8.3 | UAT_MASTER_SCENARIOS.md | E-commerce, POS, Accommodation, Hybrid; acceptance + abuse tests | — |
| 8.4 | Final validation: no PageRequest offset; no domain/repository or domain/port; build + tests pass | — | All |

---

## Dependency Graph (simplified)

```
1.1, 1.2, 1.3 → 1.4
1.1 → 5.3
1.2 → 7.1
1.3, 1.4 → (capability enforcement in controllers)
2.1 → 2.5, 3.2
2.x → 2.12 (indexes can run in parallel per module)
3.x (security) can run after 2.1 where needed
4.x (business) largely independent
5.1 → 5.2
6.1, 6.2 → 6.3
7.x depends on cursor/contract helper and security tasks
8.x final docs and validation
```

---

## Risk Summary

| Risk level | Count | Mitigation |
|------------|-------|------------|
| High | 12 tasks | Execute in order; test after each; rollback plan per module |
| Medium | 18 tasks | Review before merge; run full test suite |
| Low | 14+ tasks | Batch where possible |

---

## Estimated Order of Execution (suggested)

1. **Week 1:** Phase 1 (foundation) + Phase 2.1–2.3 (payment/customer/inventory ports) + Phase 3.1 (auth tenant tests)
2. **Week 2:** Phase 2.4–2.12 (cursor migration + indexes) + Phase 3.2–3.5 (security)
3. **Week 3:** Phase 4 (business logic) + Phase 5 (search + observability) + Phase 6 (package-info + JPA guide)
4. **Week 4:** Phase 7 (tests) + Phase 8 (docs + validation)

---

*End of MASTER_EXECUTION_PLAN.md*
