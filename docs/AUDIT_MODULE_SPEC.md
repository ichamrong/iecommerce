# Audit Module Specification — Enterprise Complete

**Version:** 1.0  
**Module:** `iecommerce-module-audit`  
**Status:** Authoritative for bank-grade audit, tamper-evidence, and cursor pagination.

---

## 1. Domain Model

### 1.1 Core Types

- **AuditEvent** (pure domain): Immutable, append-only. Fields: id, tenantId, createdAt, correlationId, actor (AuditActor), eventType, outcome (AuditOutcome), severity (AuditSeverity), target (AuditTarget), sourceModule, sourceEndpoint, ipAddress, userAgent, metadataJson, prevHash, hash.
- **AuditActor**: actorId, actorType, role.
- **AuditTarget**: targetType, targetId.
- **AuditSeverity**: INFO, WARN, CRITICAL.
- **AuditOutcome**: SUCCESS, FAILURE.
- **AuditEventType**: Enum of stable codes (e.g. ORDER_CONFIRM, PAYMENT_CAPTURE, PRODUCT_CREATE).

### 1.2 Mandatory Fields (per record)

| Field          | Required | Notes                                      |
|----------------|----------|--------------------------------------------|
| tenantId       | Yes      | From TenantContext only                    |
| correlationId  | Yes      | From MDC (request/trace id)                |
| actorId        | Yes      | From security context                      |
| actorType      | Yes      | USER, SYSTEM, SERVICE                      |
| eventType      | Yes      | Stable code                                |
| outcome        | Yes      | SUCCESS, FAILURE                           |
| severity       | Yes      | INFO, WARN, CRITICAL                       |
| targetType     | Yes      | e.g. ORDER, PAYMENT                        |
| targetId       | No       | Stable identifier                          |
| sourceModule   | No       | Calling module                             |
| sourceEndpoint | No       | Endpoint/method                            |
| ipAddress      | No       | Max 45 chars; privacy-aware                |
| userAgent      | No       | Max 500 chars                              |
| metadataJson   | No       | Max 8KB; PII-scrubbed, validated           |
| prevHash       | No       | First event in chain has null              |
| hash           | Yes      | SHA-256 of canonical form + prevHash      |

### 1.3 Privacy and Limits

- **No PII** stored unless explicitly allowed and masked.
- **metadata_json** max 8KB (AuditPolicy.METADATA_JSON_MAX_BYTES).
- **ip_address** max 45, **user_agent** max 500.

---

## 2. Event Types Catalog (Stable Codes)

- Catalog: PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_PUBLISH, PRODUCT_ARCHIVE, PRODUCT_DELETE, CATEGORY_*, COLLECTION_*, FACET_*.
- Auth: USER_REGISTER, USER_LOGIN, USER_LOGIN_FAILED, USER_DISABLE, TENANT_*.
- Customer: CUSTOMER_CREATE, CUSTOMER_UPDATE, CUSTOMER_BLOCK, CUSTOMER_ADDRESS_*.
- Staff: STAFF_CREATE, STAFF_SUSPEND, STAFF_TERMINATE, STAFF_REACTIVATE.
- Order/Booking: ORDER_COMPLETE, BOOKING_CONFIRM.
- Payment: PAYMENT_SUCCEED, PAYMENT_FAIL.
- Storage: STORAGE_UPLOAD, STORAGE_DOWNLOAD, STORAGE_DELETE.
- Generic: CUSTOM.

---

## 3. Tamper-Evidence Strategy (Hash Chain)

- **Option A (implemented):** Per-tenant hash chain.
  - Each event stores **prevHash** (hash of previous event for that tenant) and **hash** = SHA-256(canonical_form). Canonical form = stable concatenation of tenantId, createdAt, correlationId, actor, eventType, outcome, severity, target, source, ip, userAgent, metadataJson, prevHash.
  - Verification: recompute hash from stored fields; compare to stored hash. Then verify prevHash equals previous event’s hash (chain link).

- **Verify endpoint:** `GET /api/v1/audit/verify/{id}` returns the event with **hashValid** true/false (and reason if invalid).

---

## 4. Retention Policy

- **Port:** AuditRetentionPolicyPort (archiveCutoff, deleteCutoff).
- **Default:** No archiving (null). Override in config for production.
- **Recommended:** 1–2 years hot; archive beyond; document legal retention (e.g. 7 years) and deletion policy.

---

## 5. API

### 5.1 Endpoints

| Method | Path                      | Auth        | Description                |
|--------|---------------------------|------------|----------------------------|
| POST   | /api/v1/audit/events      | AUDIT_WRITE| Record event (internal/S2S)|
| GET    | /api/v1/audit/events      | AUDIT_READ | Cursor-paginated list      |
| GET    | /api/v1/audit/events/{id} | AUDIT_READ | Get by id (tenant-scoped)  |
| GET    | /api/v1/audit/verify/{id} | AUDIT_READ | Tamper verification        |

- **Tenant:** From TenantContext only (no tenantId in request body for scoping).
- **Cursor:** Sort `created_at DESC, id DESC`. filterHash binds to endpoint + filters; mismatch returns 400 INVALID_CURSOR_FILTER_MISMATCH.

### 5.2 Filters (cursor-safe)

- actorId, eventType, outcome, severity, targetType, targetId, dateFrom, dateTo, searchTerm.

### 5.3 Example: Record event (curl)

```bash
curl -X POST https://api.example.com/api/v1/audit/events \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "ORDER_CONFIRM",
    "outcome": "SUCCESS",
    "severity": "INFO",
    "targetType": "ORDER",
    "targetId": "ord-123",
    "sourceModule": "order",
    "sourceEndpoint": "POST /orders/confirm",
    "metadataJson": "{}"
  }'
```

### 5.4 Example: List with cursor

```bash
curl "https://api.example.com/api/v1/audit/events?limit=20&eventType=PAYMENT_CAPTURE"
# Use nextCursor from response for next page.
```

### 5.5 Example: Verify tamper

```bash
curl "https://api.example.com/api/v1/audit/verify/42"
# Response includes hashValid: true | false.
```

---

## 6. UAT Scenarios

1. **Tenant isolation:** Tenant A cannot read tenant B’s event (GET by id or verify → 404).
2. **Cursor contract:** Same filters + cursor → same page; cursor from different filters → 400 INVALID_CURSOR_FILTER_MISMATCH; no duplicates across pages.
3. **Tamper proof:** Modify stored event in DB → verify returns hashValid false.
4. **Authorization:** No AUDIT_READ → 403 on read; no AUDIT_WRITE → 403 on POST.
5. **Keyset index:** List query uses (tenant_id, created_at DESC, id DESC).

---

## 7. Definition of Done Checklist

- [x] Module follows folder structure (api, application/command/query/usecase/dto, domain/model/event/ports/policy/service/exception, infrastructure/config/persistence/jpa, outbox, tamper).
- [x] Ports under domain/ports only (AuditEventRepositoryPort, AuditTamperProofPort, AuditRetentionPolicyPort, AuditPublisherPort).
- [x] Cursor pagination with CursorCodec + FilterHasher + CursorPageResponse; filterHash mismatch → 400.
- [x] Multi-tenant safe: every query tenant-scoped; IDOR check on GET by id and verify.
- [x] Tamper proof: hash chain implemented; verify endpoint returns hashValid.
- [x] Liquibase: audit_event table + keyset and filter indexes.
- [x] Tests: tenant isolation, cursor filter mismatch, tamper verification; `mvn -pl iecommerce-module-audit test` passes.
- [x] No controller accepts tenantId in request for scoping; TenantContext only.
- [x] Permissions: AUDIT_READ (view), AUDIT_WRITE (record).

---

*End of AUDIT_MODULE_SPEC.md.*
