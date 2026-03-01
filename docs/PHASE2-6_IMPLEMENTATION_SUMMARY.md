# Phase 2–6 Implementation Summary

**Date:** 2025-03-01  
**Source:** `docs/SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md`, `docs/IMPLEMENTATION_CHECKLIST_CURSOR_AND_SAFETY.md`

---

## A) What Changed (by phase)

### Phase 2 — Sale module migrated to shared cursor
- **2.1–2.4** List endpoints (shifts, sessions, quotations, returns) now use:
  - `CursorCodec` + `CursorPayload` for encode/decode (Base64URL JSON with `v`, `createdAt`, `id`, `filterHash`).
  - `FilterHasher.computeHash(endpointKey, filters)` so cursors are endpoint-bound; `decodeAndValidateFilter` rejects mismatched filter → 400 `INVALID_CURSOR_FILTER_MISMATCH`.
  - `CursorPageResponse<T>` with `data`, `nextCursor`, `hasNext`, `limit`.
  - Keyset query: `(tenant_id, created_at < cursor OR (created_at = cursor AND id < cursorId)) ORDER BY created_at DESC, id DESC LIMIT limit+1`; no offset.
- **2.5** Sale keyset indexes already present in `changelog-v25-persistence-hardening.xml` (no new Liquibase).
- **2.6** Sale module has no pre-existing `src/test`; cursor behaviour covered by `iecommerce-common` tests (roundtrip, filter mismatch). Integration tests for sale list endpoints can be added in a follow-up (see “Remaining gaps”).

### Phase 3 — Tenant safety + IDOR
- **3.1** `TenantContextFilter` (auth) now:
  - Blocks **SUSPENDED**, **TERMINATED**, **DISABLED** with 403 and `X-Error-Code`: `TENANT_SUSPENDED` / `TENANT_TERMINATED`.
  - **GRACE**: allows only GET/HEAD/OPTIONS; POST/PUT/PATCH/DELETE return 403 with `TENANT_GRACE_READ_ONLY`.
- **TenantStatus** (auth): added **GRACE**, **SUSPENDED**, **TERMINATED**; **DISABLED** deprecated (kept for compatibility).
- **SubscriptionStatus** (subscription): added **GRACE**.
- **3.2** `TenantGuard.requireSameTenant(entityTenantId, currentTenantId)` added in `iecommerce-common/security`; throws 404 when tenant does not match (no IDOR leak).
- Sale controllers now use `TenantContext.requireTenantId()` (from JWT) for all list and single-resource operations; **X-Tenant-Id** header no longer used for authorization (tenant from token only).

### Phase 4 — Unified folder structure (ports)
- **4.1** Sale: `domain/repository` removed; all repository interfaces moved to **`domain/ports`** (`ShiftRepositoryPort`, `SaleSessionRepositoryPort`, `QuotationRepositoryPort`, `SaleReturnRepositoryPort`). Ports now expose `findPage(..., cursorCreatedAt, cursorId, limitPlusOne)` returning `List<Entity>`; cursor encoding/decoding lives in application layer.
- **4.2** Invoice: **`domain/port`** renamed to **`domain/ports`**; all 10 port interfaces and imports updated.
- **4.3** Promotion: **`domain/port`** renamed to **`domain/ports`**; all 3 port interfaces and imports updated.
- **4.4** Order: No duplicate repository interfaces were removed in this pass; order already uses `domain/ports` (e.g. `OrderRepositoryPort`). Any legacy `OrderRepository` in domain root can be removed in a follow-up if still present.
- **Validation:** No remaining `domain/repository` or `domain/port` packages under sale, invoice, promotion.

### Phase 5 — package-info and null safety
- **Done:** `iecommerce-common/pagination`, `sale/domain/ports`, `invoice/domain/ports`, `promotion/domain/ports` have `package-info.java`.
- **Not done:** Systematic addition of `package-info.java` to every package in every module and full null-safety pass (`@NonNullApi`/`@NonNullFields` everywhere, `@Version` and init fixes). Left for a follow-up.

### Phase 6 — Security hardening
- **Done:** Tenant status enforcement and tenant-scoping via `TenantContext`; `InvalidCursorException` → 400 with `errorCode` in body; global handler in `GlobalExceptionHandler`.
- **Not done:** Payment webhook verification/dedup audit, invoice verification endpoint, promotion redemption idempotency tests, sale saga/outbox resilience tests, observability (correlation id, structured logs). Left for follow-up.

---

## B) Files Added/Modified (high-level)

| Area | Added | Modified |
|------|--------|----------|
| **Common** | `common/pagination/*` (Phase 1 – already done), `common/security/TenantGuard.java`, `GlobalExceptionHandler` (InvalidCursorException) | — |
| **Sale** | `sale/domain/ports/*` (4 ports + package-info), `createdAt` on Shift/SaleSession/SaleReturn domain | SaleController, QuotationController, ReturnController (CursorPageResponse, TenantContext), SaleQueryService (CursorCodec, FilterHasher, CursorPageResponse), JPA adapters + Spring Data repos (findPage, List), domain models (createdAt), mapper |
| **Auth** | — | TenantContextFilter (status + GRACE read-only), TenantStatus (GRACE, SUSPENDED, TERMINATED), Tenant (updateStatus/provision) |
| **Subscription** | — | SubscriptionStatus (GRACE) |
| **Invoice** | `invoice/domain/ports/package-info.java` | All `domain/port` → `domain/ports` (moved + import updates) |
| **Promotion** | `promotion/domain/ports/package-info.java` | All `domain/port` → `domain/ports` (moved + import updates) |

---

## C) How to Run Tests

```bash
# Full build
cd /Users/chamrong/Documents/Projects/chamrong.me/iecommerce/iecommerce-api
mvn clean compile

# Common (pagination + TenantGuard)
mvn -pl iecommerce-common test

# Sale module
mvn -pl iecommerce-module-sale test

# All tests
mvn test
```

Liquibase: no new changeSets were added; sale keyset indexes are in `changelog-v25-persistence-hardening.xml`. Validate with:

```bash
mvn -pl iecommerce-app liquibase:status
```

---

## D) Remaining Gaps (minimal)

1. **Sale list integration tests (2.6)**  
   Add integration tests for sale list endpoints: deterministic ordering, page1+page2 no duplicates, filter mismatch 400, tenant isolation, concurrent insert. Sale module currently has no `src/test`; add a test class and (if needed) testcontainers or embedded DB.

2. **Order module**  
   Confirm there is no duplicate `OrderRepository` (or similar) in `order/domain` root; keep only interfaces in `order/domain/ports`.

3. **Phase 5**  
   Add `package-info.java` to every package across all modules; apply `@NonNullApi`/`@NonNullFields` and fix null-safety / `@Version` where needed.

4. **Phase 6**  
   Payment: webhook signature + dedup verification and tests. Invoice: verification endpoint and immutability tests. Promotion: redemption idempotency tests. Sale: saga/outbox and idempotency tests. Observability: correlation id and structured logging.

5. **Liquibase**  
   If any other module adds cursor-based list endpoints, add keyset indexes using the template in `docs/IMPLEMENTATION_CHECKLIST_CURSOR_AND_SAFETY.md`.

---

*End of summary.*
