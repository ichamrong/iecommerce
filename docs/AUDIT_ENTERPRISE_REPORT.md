# Enterprise Audit Report — iecommerce-api

**Version:** 1.0  
**Date:** 2025-03-01  
**Scope:** All modules under `iecommerce-api` (SaaS multi-tenant e‑commerce, POS, accommodation).  
**References:** `docs/SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md`, `docs/IMPLEMENTATION_CHECKLIST_CURSOR_AND_SAFETY.md`.

---

## Executive Summary

This report documents the enterprise audit of the iecommerce-api codebase against multi-tenant safety, cursor-only pagination, ASVS L1/L2, bank-grade reliability, clean architecture (DDD + hexagonal), and consistent folder structure. Findings are classified Critical / High / Medium / Low. Remediation is tracked in `docs/AUDIT_REMEDIATION_PLAN.md` and implemented phase-by-phase.

---

## 1) Multi-Tenant Safety

| Finding | Severity | Module(s) | Evidence | Status |
|--------|----------|-----------|----------|--------|
| TenantGuard applied on get-by-id | — | sale, order, invoice, customer, catalog, promotion, audit | `TenantGuard.requireSameTenant` in use cases/handlers | ✅ Done |
| TenantGuard missing on get-by-id | High | payment, staff, booking | Single-resource load without explicit tenant check | Remediate |
| Tenant lifecycle (GRACE/SUSPENDED/TERMINATED) | — | auth | `TenantContextFilter` blocks SUSPENDED/TERMINATED; GRACE read-only | ✅ Done |
| tenantId from JWT only | — | All | Controllers use `TenantContext.requireTenantId()`; not from body | ✅ Done |

**Evidence (payment/staff/booking):** Payment intent get-by-id, staff get-by-id, booking get-by-id must call `TenantGuard.requireSameTenant(resource.getTenantId(), TenantContext.requireTenantId())` after load.

---

## 2) Cursor Pagination (No Offset)

| Finding | Severity | Module(s) | Evidence | Status |
|--------|----------|-----------|----------|--------|
| Sale list endpoints | — | sale | `CursorCodec`, `FilterHasher`, `CursorPageResponse`; filterHash validation | ✅ Done |
| Order list | High | order | `JpaOrderAdapter` uses `PageRequest.of(0, limit)` for first/next page | Remediate |
| Invoice list | High | invoice | `JpaInvoiceRepositoryAdapter` uses `PageRequest.of(0, limit)`; need CursorCodec in API | Remediate |
| Audit list | High | audit | `JpaAuditRepositoryImpl` uses `PageRequest.of(0, limitPlusOne, KEYSET_SORT)`; API must use CursorCodec + filterHash | Remediate |
| Promotion list | High | promotion | `PromotionPersistenceAdapter` uses `PageRequest.of(0, limit+1)`; lastId-only cursor | Remediate |
| Auth users list | Medium | auth | Keyset sort; ensure CursorCodec + FilterHasher in API | Remediate |
| Catalog categories/products | Medium | catalog | Keyset in place; standardize to CursorCodec + CursorPageResponse | Remediate |
| Staff list | Medium | staff | Keyset; standardize cursor format | Remediate |
| Payment intents list | Medium | payment | PageRequest.of in adapter | Remediate |
| Inventory ledger/reservations | Low | inventory | Internal/batch use of PageRequest; list APIs if any to use cursor | Check |
| Outbox/scheduler batch | — | order, payment, promotion, etc. | `PageRequest.of(0, batchSize)` for relay — acceptable (not user list) | ✅ OK |

**Liquibase keyset indexes:** Sale (v25) ✅. Order, invoice, promotion, customer, audit, booking, auth (v26–v28) ✅. Payment intents, subscription list: add if missing.

---

## 3) Folder Structure (Ports & Adapters)

| Finding | Severity | Module(s) | Evidence | Status |
|--------|----------|-----------|----------|--------|
| domain/ports (sale, order, invoice, promotion) | — | sale, order, invoice, promotion | Repository interfaces in `domain/ports` | ✅ Done |
| domain repository in root | High | audit | `audit/domain/AuditRepository.java` | Move to `domain/ports/AuditRepositoryPort.java` |
| Duplicate repository in domain root | High | invoice | `invoice/domain/InvoiceRepository.java`; also `domain/ports/InvoiceRepositoryPort` | Consolidate into ports; remove domain root interface |
| domain/repository (singular port) | — | — | None found (invoice/promotion use `ports`) | ✅ OK |

---

## 4) Security (ASVS L1 / L2)

| Area | L1 (all modules) | L2 (auth, payment, invoice, promotion, staff) | Notes |
|------|------------------|---------------------------------------------|--------|
| Input validation | Bean validation on DTOs | — | Ensure all API inputs validated |
| IDOR | TenantGuard on resource load | — | Extend to payment, staff, booking |
| Tenant lifecycle | TenantContextFilter | — | ✅ |
| Rate limiting | RateLimitingFilter in common | Auth, payment endpoints | Verify applied |
| Idempotency | Payment intent, order confirm/ship/cancel, sale, invoice | Refund, webhook dedup | Document and verify |
| Webhook signature + replay | — | Payment (Stripe/Bakong) | Verify WebhookDeduplicationPort |
| Invoice signature | — | Verify API exposed | ✅ Spec |
| Audit logging | Audit module | Sensitive actions | ✅ |

---

## 5) Bank-Grade Reliability

| Item | Status | Notes |
|------|--------|------|
| Outbox per bounded context | ✅ | order, sale, invoice, promotion, payment, inventory, customer |
| Saga + compensation | ✅ | Sale, order, auth tenant provisioning |
| Optimistic locking (@Version) | ✅ | Order, invoice, etc. |
| Idempotency keys | ✅ | Payment intent, order confirm, invoice create |
| Retry/backoff for outbox relay | Verify | AbstractOutboxRelay / per-module scheduler |

---

## 6) Code Quality

| Finding | Severity | Action |
|--------|----------|--------|
| @Data on entities | Avoid | Prefer @Getter; @Setter only when required |
| @Version misuse | Check | Ensure not final; no NonNullFields init issues |
| package-info.java | Medium | Add to every package (api, application, domain, infrastructure) |
| Magic strings | Low | Use constants for error codes, endpoint keys |
| Logging | — | @Slf4j; no PII; correlationId/tenantId in MDC |
| Domain free of Spring/JPA | — | domain/model and domain/event must have zero Spring/jakarta.persistence imports |

---

## 7) Module-by-Module Summary

| Module | Critical/High | Remediation |
|--------|----------------|-------------|
| **iecommerce-common** | — | CursorCodec, FilterHasher, CursorPageResponse, TenantGuard ✅ |
| **sale** | — | Cursor + filterHash + TenantGuard ✅; structure ✅ |
| **order** | Cursor API layer; TenantGuard | Migrate list to CursorCodec; ensure TenantGuard on all get-by-id |
| **invoice** | Cursor API; duplicate repo | CursorCodec in list API; consolidate InvoiceRepository → ports |
| **audit** | Repository in domain root | Move AuditRepository → domain/ports (AuditRepositoryPort) |
| **promotion** | Cursor format | Standardize to CursorCodec + filterHash |
| **payment** | TenantGuard on get-by-id | Add TenantGuard after loading payment intent |
| **staff** | TenantGuard on get-by-id | Add TenantGuard after loading staff |
| **booking** | TenantGuard on get-by-id | Add TenantGuard after loading booking |
| **customer** | — | TenantGuard ✅ |
| **catalog** | — | TenantGuard ✅; cursor standardize |
| **auth** | — | TenantContextFilter lifecycle ✅; user list cursor |
| **inventory** | — | Internal batch OK; list API if any → cursor |
| **subscription** | — | List → cursor if applicable |
| **setting, report, notification, asset, chat, review** | Low | package-info; structure consistency |

---

## 8) Liquibase

| Changelog | Content | Status |
|-----------|---------|--------|
| v25 | Sale keyset indexes | ✅ |
| v26 | Auth keyset | ✅ |
| v27 | Customer, audit keyset | ✅ |
| v28 | Booking keyset | ✅ |
| (new if needed) | Payment intents, subscription list keyset | Add if tables missing |

---

*This report is updated as remediation is applied. See AUDIT_REMEDIATION_PLAN.md for phase-by-phase tasks and file-level changes.*
