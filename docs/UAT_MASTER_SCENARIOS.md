# UAT Master Scenarios

**Version:** 1.0  
**Purpose:** Acceptance criteria and security abuse tests for E-commerce, POS, Accommodation, and Hybrid flows.  
**Reference:** SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md §1.3; MASTER_EXECUTION_PLAN.md Phase 7.

---

## 1. E-commerce canonical flow

### 1.1 Happy path

| Step | Action | Acceptance criteria |
|------|--------|---------------------|
| 1 | Browse catalog (cursor list) | 200; data, nextCursor, hasNext, limit; sort created_at DESC, id DESC |
| 2 | Search product by SKU/barcode/name | 200; tenant-scoped; safe query (length limit, no leading wildcard abuse) |
| 3 | Check inventory availability | 200; quantity available per SKU/location |
| 4 | Create order (idempotency key) | 201; order in AddingItems; same key returns same order |
| 5 | Add line items | 200; order total updated |
| 6 | Apply promotion/voucher | 200; discount applied; stacking rules enforced |
| 7 | Create payment intent | 201; idempotency key returns same intent |
| 8 | Capture payment | 200; ledger entries posted exactly once |
| 9 | Issue invoice | 200; invoice immutable after issue; signed |
| 10 | Email invoice PDF | 200; verification link/metadata in email |
| 11 | Optional: refund (partial/full) | 200; refund state; ledger credit; idempotent |

### 1.2 Security / abuse tests

- **IDOR:** Tenant A cannot fetch or modify Tenant B’s order/invoice/payment by ID (403/404).
- **Cursor tampering:** Cursor from filter F1 used with filter F2 → 400 INVALID_CURSOR_FILTER_MISMATCH.
- **Duplicate payment:** Same idempotency key returns same response; no double capture.
- **Invoice immutability:** PATCH/PUT on issued invoice → 403 or 409.

---

## 2. POS canonical flow

### 2.1 Happy path

| Step | Action | Acceptance criteria |
|------|--------|---------------------|
| 1 | Open shift (RBAC) | 200; shift open; terminal/staff bound |
| 2 | Create sale session | 200; session linked to shift |
| 3 | Add items (fast lookup by SKU/barcode) | 200; line items; inventory check |
| 4 | Complete sale (idempotent) | 200; session closed; inventory relieved; idempotency key supported |
| 5 | Generate receipt/invoice | 200; receipt/invoice issued |
| 6 | Close shift + reconciliation report | 200; shift closed; report available |

### 2.2 Security / abuse tests

- **Tenant isolation:** Tenant A cannot open shift or see sessions of Tenant B.
- **Concurrent close:** Optimistic locking on shift/session; 409 on conflict.
- **Idempotency:** Same completion key → same response; no double inventory relief.

---

## 3. Accommodation canonical flow

### 3.1 Happy path

| Step | Action | Acceptance criteria |
|------|--------|---------------------|
| 1 | Search availability (date range, room type) | 200; tenant-scoped; cursor if list |
| 2 | Create booking | 201; idempotent; deposit handling if configured |
| 3 | Modify booking (rules) | 200; policy allows modification; state transition valid |
| 4 | Check-in | 200; booking state updated; audit logged |
| 5 | Add-on services (order lines) | 200; linked to booking |
| 6 | Deposit / partial capture | 200; idempotent; ledger correct |
| 7 | Check-out | 200; booking completed |
| 8 | Invoice at checkout (signed) | 200; immutable; verification available |
| 9 | Partial refund (policy-driven) | 200; within policy; ledger credit |

### 3.2 Security / abuse tests

- **Tenant isolation:** Tenant A cannot access Tenant B’s booking.
- **Double check-in:** Idempotency or state check prevents duplicate check-in.
- **Refund beyond policy:** Refund amount/eligibility enforced; 400/403 if violated.

---

## 4. Hybrid flow

### 4.1 Happy path

| Step | Action | Acceptance criteria |
|------|--------|---------------------|
| 1 | POS sale during active booking | 200; sale and booking both tenant-scoped |
| 2 | Inventory conflict policy | Deterministic: e.g. POS immediate > reservation > eCommerce; or documented priority |
| 3 | Conflict scenario: same SKU reserved (eCommerce) and sold at POS | Correct user-facing error (e.g. 409 or clear message); no double-sell |

### 4.2 Security / abuse tests

- **Module gate:** Tenant with vertical_mode ECOMMERCE only → 403 MODULE_DISABLED for /api/sale/*, /api/booking/*.
- **Module gate:** Tenant with vertical_mode HYBRID → all module endpoints allowed.

---

## 5. Cross-cutting acceptance criteria

- **Tenant status:** ACTIVE/TRIAL → allowed; SUSPENDED/TERMINATED → 403; GRACE → GET allowed, POST/PUT/PATCH/DELETE → 403.
- **Cursor lists:** All list endpoints return CursorPageResponse; no offset; filterHash enforced.
- **Logging:** No PII in logs; tenantId and requestId/correlationId present in structured form.
- **Liquibase:** All changelogs validate; keyset indexes present for list tables.

---

## 6. Implementation notes for test automation

- **Contract test helper:** Reusable assertions for cursor list (ordering, no duplicates, filter mismatch 400, tenant isolation). See iecommerce-common test scope.
- **Integration tests:** Per-module critical path (sale list, order+payment+invoice flow, webhook replay, tenant isolation).
- **UAT scenarios:** Execute at least E-commerce happy path and POS happy path as integration tests; Accommodation and Hybrid as documented manual or automated scenarios.

---

*End of UAT_MASTER_SCENARIOS.md*
