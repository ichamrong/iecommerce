# Search and Query Standards

**Version:** 1.0  
**Purpose:** Safe, tenant-scoped search patterns across catalog, customer, order, sale, booking.  
**Reference:** SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md; MODULE_COMPLETENESS_MATRIX.md.

---

## 1. Principles

- **Tenant-scoped:** Every search MUST include `tenant_id` (from TenantContext); never from request body for scope.
- **Cursor pagination:** All search/list results use keyset pagination (created_at DESC, id DESC); no offset.
- **Safe query patterns:** No leading wildcard (e.g. `%query`) in LIKE to avoid full table scan; limit input length; parameterized queries only.
- **Indexes:** Search filters used in WHERE must have supporting indexes (see DB_SCHEMA_RULES_AND_INDEXES.md).

---

## 2. Search by module

### 2.1 Catalog

| Search type | Parameters | Pattern | Index |
|-------------|------------|---------|--------|
| By SKU | tenantId, sku (exact) | equality | (tenant_id, sku) unique |
| By barcode | tenantId, barcode (exact) | equality | (tenant_id, barcode) or variant index |
| By name | tenantId, q (min 1 char, max 100) | ILIKE/LOWER LIKE 'q%' or full-text | (tenant_id, name) or tsvector |
| List products | tenantId, cursor, limit, optional category/type | keyset | (tenant_id, created_at DESC, id DESC) |

- **Safe:** Trim and limit `q` length (e.g. 100); avoid `%q%` at start; prefer prefix or full-text.

### 2.2 Customer

| Search type | Parameters | Pattern | Index |
|-------------|------------|---------|--------|
| By name | tenantId, q (max 100) | ILIKE/LOWER prefix or full-text | (tenant_id, name) |
| By email | tenantId, email (exact or normalized) | equality | (tenant_id, email) |
| By phone | tenantId, phone (normalized) | equality or prefix | (tenant_id, phone) |
| List customers | tenantId, cursor, limit | keyset | (tenant_id, created_at DESC, id DESC) |

- **PII:** Do not log raw email/phone; use masked or hash in logs.

### 2.3 Order

| Search type | Parameters | Pattern | Index |
|-------------|------------|---------|--------|
| By code | tenantId, code (exact) | equality | (tenant_id, code) unique |
| By status | tenantId, status, cursor, limit | keyset + status filter | (tenant_id, status, created_at DESC, id DESC) |
| By customer | tenantId, customerId, cursor, limit | keyset + customerId | (tenant_id, customer_id, created_at DESC, id DESC) |
| By date range | tenantId, from, to, cursor, limit | keyset + created_at range | keyset index |

### 2.4 Sale

| Search type | Parameters | Pattern | Index |
|-------------|------------|---------|--------|
| By shift | tenantId, shiftId (or list by shift) | equality / keyset | (tenant_id, created_at DESC, id DESC) on sessions |
| By session | tenantId, sessionId | equality | PK / tenant_id |
| By date | tenantId, from, to, cursor, limit | keyset + created_at range | keyset index |
| List shifts/sessions/returns/quotations | tenantId, cursor, limit, optional filters | keyset + filterHash | (tenant_id, created_at DESC, id DESC) |

### 2.5 Booking

| Search type | Parameters | Pattern | Index |
|-------------|------------|---------|--------|
| By guest | tenantId, guest/customer id, cursor, limit | keyset | (tenant_id, created_at DESC, id DESC) or (tenant_id, customer_id, ...) |
| By date/status | tenantId, from, to, status, cursor, limit | keyset + filters | keyset index |
| Availability | tenantId, from, to, room/slot type | range + availability rules | (tenant_id, start_date, end_date) etc. |

---

## 3. Implementation checklist

- [ ] Catalog: search by sku/barcode/name with length limit and safe LIKE (prefix or parameterized).
- [ ] Customer: search by name/email/phone; cursor list; no PII in logs.
- [ ] Order: search by code/status/customer/date; cursor; filterHash if multiple filters.
- [ ] Sale: search by shift/session/date; cursor already in place; document.
- [ ] Booking: search by guest/date/status; cursor; indexes.

---

## 4. Postgres full-text (optional)

- Use `to_tsvector` / `to_tsquery` for product name or customer name search when prefix search is insufficient.
- Maintain tsvector column or expression index; keep tenant_id in index.

---

*End of SEARCH_AND_QUERY_STANDARDS.md*
