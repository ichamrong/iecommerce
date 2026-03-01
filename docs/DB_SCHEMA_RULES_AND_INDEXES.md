# Database Schema Rules and Indexes

**Version:** 1.0  
**Purpose:** Liquibase discipline, keyset indexes, unique constraints, retention.  
**Reference:** SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md §6; MODULE_COMPLETENESS_MATRIX.md.

---

## 1. Index strategy

### 1.1 Primary keys

- All tables have PK `id` (bigint/serial or UUID per module convention).

### 1.2 Keyset (cursor list) index

For every list endpoint that uses cursor pagination:

```sql
(tenant_id, created_at DESC, id DESC)
```

**Liquibase template:**

```xml
<createIndex tableName="<table>" indexName="idx_<table>_keyset">
  <column name="tenant_id"/>
  <column name="created_at" descending="true"/>
  <column name="id" descending="true"/>
</createIndex>
```

**Tables (per module):** sale (shifts, sessions, quotations, returns — v25); order; invoice; payment_intent; promotion; catalog (product, category); staff; inventory (ledger, reservation); booking; subscription; customer; audit (if list).

### 1.3 Lookups

- Unique on `(tenant_id, business_key)` where applicable: order code, invoice number, product slug, etc.
- Index FK columns used in joins and filters.

---

## 2. Unique constraints

| Purpose | Pattern | Example |
|---------|---------|--------|
| Idempotency | (tenant_id, idempotency_key) or (idempotency_key) global | payment_intent, invoice create |
| Business key | (tenant_id, code) / (tenant_id, number) / (tenant_id, slug) | order, invoice, product |
| Redemption | (tenant_id, promotion_id, customer_id or order_id) | promotion redemption |
| Webhook dedup | (event_id) or (tenant_id, event_id) | payment webhook |

---

## 3. Foreign keys

- Enforce referential integrity where required (order → customer, invoice → order, etc.).
- Index FK columns used in WHERE/JOIN.

---

## 4. Liquibase discipline

- Single master changelog; include module-specific changelogs by version.
- One changeSet per logical change; idempotent; use preConditions if needed.
- No destructive changes without migration path (e.g. rename column: copy + drop).
- Cursor indexes in dedicated changeSet (e.g. v25-style).

---

## 5. Retention and archiving

- **Audit / finance:** Retain per regulation (e.g. 7 years); document retention per entity.
- Hot data in main tables; archive old orders/invoices/audit to archive tables or cold storage by date.
- Soft delete: Tenant, User (block login; retain for audit). Hard delete only for GDPR purge; else anonymize + soft delete.

---

## 6. Module index checklist

| Module | Table(s) | Keyset index | Unique (business / idempotency) |
|--------|----------|--------------|----------------------------------|
| sale | sales_shifts, sales_sessions, sales_quotations, sale_returns | v25 | shift/session codes |
| order | order | Add if missing | (tenant_id, code) |
| invoice | invoice | Add if missing | (tenant_id, number) |
| payment | payment_intent | Add if missing | idempotency_key |
| promotion | promotion | Add if missing | redemption key |
| catalog | product, category | Add if missing | (tenant_id, slug) |
| staff | staff | Add if missing | (tenant_id, ...) |
| inventory | ledger, reservation | Add if missing | — |
| booking | booking | Add if missing | — |
| customer | customer | Add if missing | (tenant_id, email) |
| subscription | tenant_subscription | Add if missing | — |

---

*End of DB_SCHEMA_RULES_AND_INDEXES.md*
