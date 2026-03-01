# Implementation Checklist — Cursor Pagination & Tenant Safety

**Source of truth:** `docs/SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md`

This checklist is prioritized and broken into small, reviewable steps. Each step includes tests and Liquibase changes where needed.

---

## 1) Prioritized Implementation Checklist (20 tasks)

### Phase 1 — Shared cursor pagination (DONE)
- [x] **1.1** Create `iecommerce-common/.../pagination` package with `CursorPayload`, `InvalidCursorException`, `CursorCodec`, `FilterHasher`, `CursorPageResponse`, `CursorPageRequest`, `package-info.java`.
- [x] **1.2** Add `CursorCodecTest` (roundtrip, invalid cursor, filter validation) and `FilterHasherTest` (stable ordering, endpoint binding, null exclusion).
- [x] **1.3** Add `CursorPageResponseTest` (lastPage, withNext, null safety).

### Phase 2 — Migrate Sale module to shared cursor
- [ ] **2.1** Identify all list endpoints in sale: `QuotationController.listQuotations`, `SaleController.listShifts`, `SaleController.listSessions`, `ReturnController.listReturns`; confirm keyset query and stable sort.
- [ ] **2.2** Replace sale’s raw `createdAt:id` cursor with `CursorCodec` encode/decode; use `CursorPageResponse` in API response DTOs.
- [ ] **2.3** Update sale repository adapters to accept `CursorPayload` (or `createdAt`+`id`) and use `WHERE (created_at < :cursorCreatedAt OR (created_at = :cursorCreatedAt AND id < :cursorId)) ORDER BY created_at DESC, id DESC LIMIT :limitPlusOne`; remove any `PageRequest`/offset.
- [ ] **2.4** Add filterHash: for endpoints with filters, compute hash via `FilterHasher.computeHash(endpointName, filters)`; include in cursor; on next request call `CursorCodec.decodeAndValidateFilter(cursor, currentFilterHash)` and return 400 with `INVALID_CURSOR_FILTER_MISMATCH` on mismatch.
- [ ] **2.5** Verify Liquibase keyset indexes for `sales_quotations`, `sales_sessions`, `sales_shifts`, `sale_returns` (v25 already has them; confirm and document).
- [ ] **2.6** Add sale integration tests: deterministic ordering, page1+page2 no duplicates, filter mismatch 400, tenant isolation, concurrent insert behavior.

### Phase 3 — Tenant safety + IDOR
- [ ] **3.1** Upgrade `TenantContextFilter`: after resolving tenant from JWT, load tenant status (from auth/subscription); if SUSPENDED/TERMINATED return 403 with stable error code; if GRACE enforce read-only (optional, document).
- [ ] **3.2** Add `TenantGuard.assertTenant(entityTenantId, currentTenantId)` in common; use in order, payment, invoice, promotion, sale after loading entity (or rely on repository tenant scope + 404).
- [ ] **3.3** Apply tenant guard to order, payment, invoice, promotion, sale single-resource endpoints (get-by-id, update, cancel, etc.).

### Phase 4 — Unified folder structure
- [ ] **4.1** Sale: move `domain/repository/*` → `domain/ports/*`; update package and all imports; add `package-info.java` in `domain/ports`.
- [ ] **4.2** Invoice: rename `domain/port` → `domain/ports`; update imports.
- [ ] **4.3** Promotion: rename `domain/port` → `domain/ports`; update imports.
- [ ] **4.4** Order: remove duplicate repository interfaces from domain root; keep only `domain/ports`; ensure infrastructure implements ports.

### Phase 5 — package-info + null safety
- [ ] **5.1** Add missing `package-info.java` in every package (api, application/command, query, usecase, dto, domain/model, event, ports, policy, service, exception, infrastructure/…); use `@NonNullApi`/`@NonNullFields` for api/application/infrastructure; domain docs only.
- [ ] **5.2** Fix null-safety: `@Version` fields not final; initialize collections; avoid NPE on optional fields.

### Phase 6 — Security (ASVS L2)
- [ ] **6.1** Rate limiting on auth and payment endpoints (verify `RateLimitingFilter` applied; add config if needed).
- [ ] **6.2** Webhook dedup and signature verification (payment): verify `WebhookDeduplicationPort` used; document Stripe/Bakong verification.
- [ ] **6.3** Invoice signing verification: expose GET endpoint to verify signature of an invoice by id.
- [ ] **6.4** Document key rotation for signing keys and API keys in `docs/`.

---

## 2) First 3 tasks — exact files to create/modify

### Task 1.1 (DONE) — Create shared pagination in common
**Created:**
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/package-info.java`
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/InvalidCursorException.java`
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/CursorPayload.java`
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/CursorCodec.java`
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/FilterHasher.java`
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/CursorPageResponse.java`
- `iecommerce-common/src/main/java/com/chamrong/iecommerce/common/pagination/CursorPageRequest.java`

**Modified:** None.

### Task 1.2 (DONE) — Add tests
**Created:**
- `iecommerce-common/src/test/java/com/chamrong/iecommerce/common/pagination/CursorCodecTest.java`
- `iecommerce-common/src/test/java/com/chamrong/iecommerce/common/pagination/FilterHasherTest.java`
- `iecommerce-common/src/test/java/com/chamrong/iecommerce/common/pagination/CursorPageResponseTest.java`

### Task 2.1 — Identify sale list endpoints and confirm keyset
**To modify / add:**
- `iecommerce-module-sale/.../api/QuotationController.java` — list method
- `iecommerce-module-sale/.../api/SaleController.java` — listShifts, listSessions
- `iecommerce-module-sale/.../api/ReturnController.java` — listReturns
- `iecommerce-module-sale/.../application/query/SaleQueryService.java`
- `iecommerce-module-sale/.../domain/ports/*` (after 4.1 move) or `.../domain/repository/*` (current) — port interfaces
- `iecommerce-module-sale/.../infrastructure/persistence/jpa/Jpa*RepositoryAdapter.java` — ensure keyset query, no offset

**To add:** Integration test class that lists shifts/sessions/quotations/returns and asserts sort and cursor shape.

---

## 3) Code skeletons (reference — full code already in repo)

### CursorPayload
- Fields: `int v`, `Instant createdAt`, `String id`, `String filterHash`.
- Constructor and getters; `validateVersion()` throws `InvalidCursorException` if `v != 1`.

### CursorCodec
- `encode(CursorPayload)` → Base64URL string (no padding).
- `decode(String)` → `CursorPayload`; throws `InvalidCursorException` (INVALID_CURSOR, INVALID_CURSOR_VERSION).
- `decodeAndValidateFilter(String cursor, String expectedFilterHash)` → decode and compare filterHash; throws INVALID_CURSOR_FILTER_MISMATCH if mismatch.
- Uses minimal JSON (no Jackson) and `Base64.getUrlEncoder().withoutPadding()`.

### FilterHasher
- `computeHash(String endpointOrModuleName, Map<String, Object> filters)` → SHA-256 hex (lowercase).
- Canonical form: TreeMap (sorted keys); only non-null values; `_endpoint` key for endpoint name.

### CursorPageResponse&lt;T&gt;
- `List<T> data`, `String nextCursor`, `boolean hasNext`, `int limit`.
- Static: `lastPage(data, limit)`, `withNext(data, nextCursor, limit)`, `of(data, nextCursor, hasNext, limit)`.

### InvalidCursorException + error codes
- `INVALID_CURSOR` — malformed/not Base64/not JSON.
- `INVALID_CURSOR_FILTER_MISMATCH` — cursor from different filters.
- `INVALID_CURSOR_VERSION` — unsupported `v`.
- `getErrorCode()` for API response.

### CursorPageRequest
- `String cursor`, `int limit` (clamped 1..100), `Map<String, Object> filters`.
- `getLimitPlusOne()` for DB limit.

---

## 4) Liquibase index changeset template

Use for any list table that does not yet have a keyset index. Sale tables already have indexes in `changelog-v25-persistence-hardening.xml`; use this template for other modules.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.3.xsd">

    <changeSet id="keyset-index-<TABLE>" author="iecommerce">
        <comment>Keyset pagination: (tenant_id, created_at DESC, id DESC)</comment>
        <createIndex tableName="<TABLE>" indexName="idx_<table>_keyset">
            <column name="tenant_id"/>
            <column name="created_at" descending="true"/>
            <column name="id" descending="true"/>
        </createIndex>
    </changeSet>
</databaseChangeLog>
```

Replace `<TABLE>` and `<table>` with the actual table name (e.g. `ecommerce_order` → `idx_ecommerce_order_keyset`). Include this changeSet in the app master changelog.

---

## 5) Exact test plan for Sale migration (Phase 2.6)

| # | Scenario | Steps | Expected |
|---|----------|--------|----------|
| 1 | Deterministic ordering (same created_at, different id) | Insert two rows same tenant, same created_at (or 1ms apart), different id. List with limit 2. | Order: higher id first (id DESC). Second page cursor must not skip or duplicate. |
| 2 | Page1 + Page2 no duplicates | List page1 limit 5; use nextCursor for page2 limit 5. Collect all IDs. | No duplicate IDs; no gap (all IDs from a single “full list” query appear in order). |
| 3 | Filter mismatch returns 400 | List with filter A; copy nextCursor. Call same endpoint with filter B and pasted cursor. | 400 Bad Request; body or header error code `INVALID_CURSOR_FILTER_MISMATCH`. |
| 4 | Tenant isolation | As tenant A: list shifts, get nextCursor. As tenant B: call list shifts with tenant A’s cursor (if ever exposed). | Either cursor rejected (filterHash includes tenantId) or returns empty/403. No tenant B data. |
| 5 | Concurrent insert between pages | Thread 1: page1. Thread 2: insert one new row. Thread 1: page2 with cursor from page1. | No duplicate rows in page1+page2; new row may appear in page2 or later (document: keyset is stable, new row can appear on next page). |

**Test classes to add (suggested):**
- `SaleCursorPaginationIntegrationTest` (or similar) in `iecommerce-module-sale/src/test`: use `@SpringBootTest` or slice with `@DataJpaTest` + repository only; create tenant, insert shifts/sessions/quotations/returns, call query service or repository with cursor, assert order and hasNext/nextCursor.
- Controller integration tests: call REST list endpoints with cursor and optional filters; assert 200 and response shape; assert 400 for filter mismatch (and optionally invalid cursor string).

---

*Phase 1 implementation is complete. Proceed to Phase 2 (Sale migration) next.*
