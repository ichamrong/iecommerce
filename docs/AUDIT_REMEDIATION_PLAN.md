# Audit Remediation Plan — Phase-by-Phase Execution

**Source:** `docs/AUDIT_ENTERPRISE_REPORT.md`, `docs/SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md`

---

## Phase 1 — Structural Enforcement & Common (DONE)

- [x] **1.1** Shared cursor in iecommerce-common: `CursorPayload`, `InvalidCursorException`, `CursorCodec`, `FilterHasher`, `CursorPageResponse`, `CursorPageRequest`, `package-info.java`.
- [x] **1.2** Tests: `CursorCodecTest`, `FilterHasherTest`, `CursorPageResponseTest`.
- [x] **1.3** TenantGuard: `requireTenantIdPresent()`, `requireSameTenant()`; TenantContextFilter: GRACE read-only, SUSPENDED/TERMINATED 403.
- [x] **1.4** Sale module: cursor + filterHash + CursorPageResponse; TenantGuard on shift/session/quotation/return.

---

## Phase 2 — Folder Structure Normalization

### 2.1 Audit module
- [ ] **2.1.1** Create `domain/ports/AuditRepositoryPort.java` (move interface from `domain/AuditRepository.java`; rename to Port).
- [ ] **2.1.2** Update `AuditService` and infrastructure to use `AuditRepositoryPort`.
- [ ] **2.1.3** Delete `domain/AuditRepository.java`.
- [ ] **2.1.4** Add `domain/ports/package-info.java` if missing.

### 2.2 Invoice module
- [ ] **2.2.1** Add `findByIdempotencyKey(String tenantId, String key)` to `InvoiceRepositoryPort`; implement in `JpaInvoiceRepositoryAdapter` and `SpringDataInvoiceRepository`.
- [ ] **2.2.2** Migrate `InvoiceService` and `PosReceiptService` to use `InvoiceRepositoryPort` (findByIdAndTenant, findByIdempotencyKey with tenantId).
- [ ] **2.2.3** Remove `domain/InvoiceRepository.java`; remove or refactor `JpaInvoiceRepository` (infrastructure) so only one adapter implements the port.

---

## Phase 3 — Cursor Pagination Everywhere (No Offset)

### 3.1 Order module
- [ ] **3.1.1** Order list API: accept cursor, limit, filters; use `CursorCodec.decodeAndValidateFilter`; compute filterHash with `FilterHasher.computeHash("order:list", filters)`; return `CursorPageResponse<OrderSummary>`.
- [ ] **3.1.2** Order query/repository: already keyset (created_at, id); ensure next cursor built with `CursorCodec.encode(CursorPayload(1, createdAt, id, filterHash))`.
- [ ] **3.1.3** Remove any offset-based list; keep internal `PageRequest.of(0, limit)` only for keyset (no OFFSET in SQL).

### 3.2 Invoice module
- [ ] **3.2.1** Invoice list API: use `CursorCodec` + `FilterHasher`; return `CursorPageResponse`; reject cursor on filterHash mismatch (400 INVALID_CURSOR_FILTER_MISMATCH).
- [ ] **3.2.2** Adapter: already keyset (issuedAt, id); wire cursor decode in application layer.

### 3.3 Audit module
- [ ] **3.3.1** Audit list API: decode cursor with `CursorCodec.decodeAndValidateFilter`; build nextCursor with `CursorCodec.encode`; return `CursorPageResponse`.
- [ ] **3.3.2** Keep keyset in `JpaAuditRepositoryImpl`; remove `PageRequest.of(0, n)` from *public* list contract (internal use of limit-only PageRequest is acceptable).

### 3.4 Promotion module
- [ ] **3.4.1** Replace lastId-only cursor with `CursorPayload` (v, createdAt, id, filterHash); use `CursorCodec` and `FilterHasher` in list endpoint; return `CursorPageResponse`.

### 3.5 Other modules (auth, catalog, staff, payment)
- [ ] **3.5.1** Auth user list: CursorCodec + filterHash + CursorPageResponse.
- [ ] **3.5.2** Catalog product/category list: standardize to CursorPageResponse + CursorCodec.
- [ ] **3.5.3** Staff list: same.
- [ ] **3.5.4** Payment intent list: same.

### 3.6 Liquibase
- [ ] **3.6.1** Ensure keyset indexes exist for: order, invoice, promotion, payment_intents, subscription (if list tables). Add changeSet in `changelog-v29-keyset-remaining.xml` or equivalent if any missing.

---

## Phase 4 — TenantGuard on All Critical Get-By-Id

- [ ] **4.1** Payment: after loading PaymentIntent by id (and tenantId), call `TenantGuard.requireSameTenant(intent.getTenantId(), TenantContext.requireTenantId())`.
- [ ] **4.2** Staff: after loading Staff by id, same.
- [ ] **4.3** Booking: after loading Booking by id, same.
- [ ] **4.4** Document: order, invoice, sale, customer, catalog, promotion already use TenantGuard.

---

## Phase 5 — Security Hardening (ASVS L2)

- [ ] **5.1** Rate limiting: confirm `RateLimitingFilter` applied to auth and payment endpoints.
- [ ] **5.2** Webhook: verify `WebhookDeduplicationPort` used in Stripe/Bakong handlers; replay protection.
- [ ] **5.3** Invoice: verify signature verification endpoint (GET) exists and is documented.
- [ ] **5.4** Docs: add key rotation and secrets handling in `docs/`.

---

## Phase 6 — Package-Info & Code Quality

- [ ] **6.1** Add `package-info.java` to every package (api, application/command, query, usecase, dto, domain/model, event, ports, policy, service, exception, infrastructure/config, persistence/jpa, outbox, saga, client). Use `@NonNullApi` + `@NonNullFields` for api/application/infrastructure; domain docs only.
- [ ] **6.2** Create `docs/JPA_ENTITY_GUIDE.md` (no @Data; @Version; Lombok policy).
- [ ] **6.3** Create `docs/NULLABILITY_AND_LOMBOK_POLICY.md`.

---

## Phase 7 — Final Validation

- [ ] **7.1** No offset pagination in list endpoints (grep PageRequest.of in list/query services; ensure only keyset limit).
- [ ] **7.2** No `domain/repository` or `domain/port` (singular); repository interfaces only in `domain/ports`.
- [ ] **7.3** Domain has no Spring/JPA imports.
- [ ] **7.4** All packages have package-info.java.
- [ ] **7.5** Liquibase validates; tests pass.

---

## Tests Added (to be filled per phase)

| Module | Test Class / Scenario | Purpose |
|--------|------------------------|---------|
| common | TenantGuardTest | requireTenantIdPresent, requireSameTenant |
| common | CursorCodecTest, FilterHasherTest, CursorPageResponseTest | Cursor roundtrip, filter hash, response |
| auth | TenantContextFilterTest | GRACE read-only, SUSPENDED/TERMINATED 403 |
| order | OrderServiceTenantGuardTest | Tenant A cannot read Tenant B order |
| sale | SaleCursorPaginationIntegrationTest | Cursor ordering, filter mismatch, tenant isolation |

---

## Commands to Run

```bash
# Full test suite
mvn -q -DskipTests=false test

# Per module
mvn -pl iecommerce-common test
mvn -pl iecommerce-module-sale test
mvn -pl iecommerce-module-order test
# ... etc.
```

---

*Update this plan as each phase is completed; link to AUDIT_ENTERPRISE_REPORT.md for findings.*
