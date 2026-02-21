# IECommerce — Module Implementation Plan

> **Purpose:** This document is the master delivery plan for completing the 6 core modules.
> It captures what already exists, what is missing, the implementation order, and the
> DDD-aligned design decisions before any code is written.
>
> **Last Updated:** 2026-02-21  
> **Status:** 🟡 In Progress

---

## 1. Current State Assessment

| Module | Domain | Application | Infrastructure | API | Migrations | Test | Overall |
|---|---|---|---|---|---|---|---|
| `auth` | ✅ Rich | ✅ Rich | ✅ Rich | ✅ Done | ⚠️ Partial | ⚠️ Partial | 🟡 70% |
| `catalog` | ✅ Rich | ✅ Rich | ✅ Rich | ✅ Done | ✅ Done | 🔴 None | 🟡 90% |
| `customer` | ✅ Rich | ✅ Rich | ✅ Rich | ✅ Done | ✅ Done | 🔴 None | 🟡 80% |
| `staff` | ✅ Rich | ✅ Rich | ✅ Rich | ✅ Done | ✅ Done | 🔴 None | 🟡 80% |
| `audit` | 🔴 Stub | 🔴 Stub | 🔴 Stub | 🔴 None | 🔴 None | 🔴 None | 🔴 15% |
| `setting` | 🟡 Basic | 🔴 Stub | 🟡 Basic | 🔴 None | 🔴 None | 🔴 None | 🟡 25% |

---

## 2. Delivery Phases

### Phase 1 — Foundation (Week 1)
**Goal:** Close all the gaps needed before any feature module can work correctly.

| # | Task | Module | Why First |
|---|---|---|---|
| 1.1 | Write Liquibase migrations for all 6 modules | All | DB schema is the bedrock |
| 1.2 | Enrich `Customer` domain (Address, Loyalty) | Customer | Referenced by Order, Promotion |
| 1.3 | Enrich `StaffProfile` domain (Role, Status) | Staff | Referenced by Auth & Audit |
| 1.4 | Wire `AuditService` listeners to all write operations | Audit | Must capture events from day one |
| 1.5 | Complete `SettingService` CRUD + REST API | Setting | Used by all other modules for quotas |

### Phase 2 — Catalog Core (Week 2)
**Goal:** A fully functional product catalog that can be queried by Order module.

> Full detail: [`docs/catalog-manage-spec.md`](catalog-manage-spec.md)

| # | Task | Notes |
|---|---|---|
| 2.1 | ✅ Migration: All catalog tables + translation tables | See Section 5 & 11.3 of spec |
| 2.2 | ✅ Domain: `ProductStatus`, `ProductType` enums | DRAFT → ACTIVE → ARCHIVED lifecycle |
| 2.3 | ✅ Domain: `Product` (locale-invariant fields only) | `name`/`description` REMOVED — in translation table |
| 2.4 | ✅ Domain: `ProductTranslation` entity | `(product_id, locale)` unique |
| 2.5 | ✅ Domain: `ProductVariant` + `ProductVariantTranslation` | `name` in translation table |
| 2.6 | ✅ Domain: `Category` (adjacency list + materialized path) | Max 5 levels |
| 2.7 | ✅ Domain: `CategoryTranslation` entity | `(category_id, locale)` unique |
| 2.8 | ✅ Domain: `Collection`, `CollectionTranslation` | Dynamic product groups |
| 2.9 | ✅ Domain: `Facet`, `FacetTranslation`, `FacetValue`, `FacetValueTranslation` | `code` stays on main table |
| 2.10 | ✅ Domain: `ProductAttribute`, `ProductRelationship` | No translation (technical specs) |
| 2.11 | ✅ Application: `CreateProductHandler` (with translations map) | Quota check via `SettingService` |
| 2.12 | ✅ Application: `UpdateProductHandler`, `ArchiveProductHandler`, `PublishProductHandler` | Lifecycle methods |
| 2.13 | ✅ Application: `UpsertProductTranslationHandler` | Upsert one locale for a product |
| 2.14 | ✅ Application: `ProductQueryHandler` (list, detail, by-slug, `?locale=`) | Returns translation for requested locale |
| 2.15 | ✅ Application: `CategoryQueryHandler` (tree, breadcrumb) | Recursive tree-building |
| 2.16 | ✅ API: `ProductController` — full CRUD + lifecycle endpoints | `/api/v1/admin/products` |
| 2.17 | ✅ API: `ProductController` — translation endpoints | `GET/PUT/DELETE /translations/{locale}` |
| 2.18 | ✅ API: `CategoryController` — CRUD + tree + move | `/api/v1/admin/categories` |
| 2.19 | ✅ API: `FacetController` — CRUD + translation endpoints | `/api/v1/admin/facets` |
| 2.20 | ✅ `CatalogApi` public interface for inter-module calls | Used by Order, Inventory |

### Phase 3 — Customer Enrichment (Week 2–3)
**Goal:** Customer profiles complete enough for Order and Loyalty use.

| # | Task | Notes |
|---|---|---|
| 3.1 | ✅ Domain: `Address` embeddable complete (billing vs shipping) | Value object |
| 3.2 | ✅ Domain: `LoyaltyTier` enum + `loyaltyPoints` on Customer | Basic loyalty |
| 3.3 | ✅ Application: `UpdateCustomerHandler`, `AddAddressHandler` | |
| 3.4 | ✅ API: GET `/api/v1/customers/{id}/addresses` + POST/DELETE | |
| 3.5 | ✅ Event: Listen to `OrderCompletedEvent` → award loyalty points | Cross-module event |

### Phase 4 — Staff Enrichment (Week 3)
**Goal:** Staff profiles fully usable for access control and audit trail.

| # | Task | Notes |
|---|---|---|
| 4.1 | ✅ Domain: `StaffStatus` enum (`ACTIVE`, `SUSPENDED`, `TERMINATED`) | |
| 4.2 | ✅ Domain: `StaffRole` enum (`STORE_MANAGER`, `SALES_AGENT`, `CASHIER`, `SUPPORT`) | Granular within staff |
| 4.3 | ✅ Application: `SuspendStaffHandler`, `ReactivateStaffHandler` | |
| 4.4 | ✅ API: `PATCH /api/v1/admin/staff/{id}/suspend` etc. | |
| 4.5 | ✅ Event: Publish `StaffSuspendedEvent` → Audit picks it up | |

### Phase 5 — Audit Module (Week 3)
**Goal:** A robust, immutable audit trail for all write operations.

| # | Task | Notes |
|---|---|---|
| 5.1 | Domain: Enrich `AuditEvent` (actor, action, entity, diff, ip) | Immutable record |
| 5.2 | Application: `AuditEventListener` — listen to all domain events | |
| 5.3 | Application: `AuditQueryHandler` — paginated search with filters | |
| 5.4 | API: `GET /api/v1/admin/audit-log` (admin only) | |
| 5.5 | Migration: `audit_events` table with composite partitioning by year | |

### Phase 6 — Setting Module (Week 4)
**Goal:** Full tenant + global settings with quota enforcement.

| # | Task | Notes |
|---|---|---|
| 6.1 | Domain: `SettingCategory` enum (`GENERAL`, `INTEGRATION`, `QUOTA`) | |
| 6.2 | Domain: Add `dataType`, `isSecret` fields to `TenantSetting` | For masking secrets |
| 6.3 | Application: `UpdateSettingHandler`, `ResetToDefaultHandler` | |
| 6.4 | Application: `QuotaEnforcer` — generic quota check service | Called by catalog, auth |
| 6.5 | API: `GET/PUT /api/v1/admin/settings` (global) | |
| 6.6 | API: `GET/PUT /api/v1/tenants/me/settings` (tenant) | |

---

## 3. Module-by-Module Detailed Gap Analysis

### 3.1 Catalog Module
**What exists:** `Product` (name, slug, description, enabled), `ProductVariant` (stub), `CatalogService` (basic CRUD)

> ⚠️ **Breaking change from existing stub:** `name` and `description` columns move OFF `catalog_products`
> into `catalog_product_translations`. The existing stub `Product.java` will be rewritten.

**What is MISSING:**

```
Domain:
  ✅ ProductStatus enum (DRAFT → ACTIVE → ARCHIVED)
  ✅ ProductType enum (PHYSICAL, DIGITAL, SERVICE)
  ✅ ProductTranslation         (name, description, short_description, meta_title, meta_description)
  ✅ ProductVariantTranslation  (variant name per locale)
  ✅ Category                   (hierarchical tree, adjacency list + materialized path)
  ✅ CategoryTranslation        (name, description per locale)
  ✅ Collection + CollectionTranslation
  ✅ Facet + FacetTranslation   (label: "Brand" → "ម៉ាក")
  ✅ FacetValue + FacetValueTranslation ("Apple" → "អាប់ផ្លោ")
  ✅ ProductAttribute           (static specs — locale-invariant, no translation table)
  ✅ ProductRelationship        (UPSELL, CROSS_SELL, RELATED, BUNDLE)
  ✅ tenantId on Product        (multi-tenancy not implemented)
  ✅ slug uniqueness            (DB UNIQUE constraint per tenant missing)

Application:
  ✅ CreateProductCommand / CreateProductHandler  (accepts translations map)
  ✅ UpdateProductCommand / UpdateProductHandler
  ✅ PublishProductHandler / ArchiveProductHandler / ReactivateProductHandler
  ✅ UpsertProductTranslationHandler
  ✅ ProductQueryHandler         (by-slug, by-category, paginated, locale-aware)
  ✅ CategoryQueryHandler        (tree, breadcrumb)
  ✅ ProductResponse DTO         (never expose domain entity directly over API)

Infrastructure:
  ✅ JpaProductRepository        (findBySlug, findByTenantAndCategory)
  ✅ JpaCategoryRepository       (findByMaterializedPathStartingWith)
  ✅ Liquibase migrations

API:
  ✅ Admin product CRUD          (POST, PUT, DELETE /api/v1/admin/products)
  ✅ Product lifecycle           (PATCH /publish, /archive, /reactivate)
  ✅ Translation endpoints       (GET/PUT/DELETE /api/v1/admin/products/{id}/translations/{locale})
  ✅ Category API                (GET /api/v1/admin/categories/tree)
  ✅ Facet + FacetValue CRUD     (with translation endpoints)
```

### 3.2 Customer Module
**What exists:** `Customer` (basic), `Address` (embedded), `CreateCustomerHandler`, `CustomerQueryHandler`, `AuthEventListener`

**What is MISSING:**
```
Domain:
  ✗ LoyaltyPoints / LoyaltyTier
  ✗ CustomerStatus enum (ACTIVE, BLOCKED)
  ✗ Multiple addresses (currently single embedded address)
  ✗ dateOfBirth, gender (for personalization)

Application:
  ✗ UpdateCustomerHandler
  ✗ BlockCustomerHandler
  ✗ AddAddressHandler / RemoveAddressHandler
  ✗ LoyaltyPointsService

API:
  ✗ PUT /api/v1/customers/{id}
  ✗ GET/POST/DELETE /api/v1/customers/{id}/addresses
  ✗ PATCH /api/v1/customers/{id}/block

Infrastructure:
  ✗ Liquibase migrations
```

### 3.3 Staff Module
**What exists:** `StaffProfile` (basic), `CreateStaffHandler`, `UpdateStaffTenantsHandler`, `StaffQueryHandler`

**What is MISSING:**
```
Domain:
  ✗ StaffStatus enum (ACTIVE, SUSPENDED, TERMINATED)
  ✗ StaffRole enum (STORE_MANAGER, SALES_AGENT, CASHIER, SUPPORT)
  ✗ hireDate, terminationDate
  ✗ department / branch assignment

Application:
  ✗ SuspendStaffHandler
  ✗ ReactivateStaffHandler
  ✗ TerminateStaffHandler

API:
  ✗ PATCH /api/v1/admin/staff/{id}/suspend
  ✗ PATCH /api/v1/admin/staff/{id}/reactivate
  ✗ PATCH /api/v1/admin/staff/{id}/terminate

Infrastructure:
  ✗ Liquibase migrations
```

### 3.4 Audit Module
**What exists:** `AuditEvent` (basic), `AuditService` (stub), `JpaAuditRepository`

**What is MISSING:**
```
Domain:
  ✗ actorId, actorName, actorRole   (who did it)
  ✗ action enum (CREATE, UPDATE, DELETE, LOGIN, etc.)
  ✗ entityType, entityId            (what was affected)
  ✗ changeDetails (JSON diff)       (before/after snapshot)
  ✗ ipAddress, userAgent            (forensics)
  ✗ Immutability enforcement        (no public setters on AuditEvent)

Application:
  ✗ AuditEventListener (Spring Event / Modulith Event)
  ✗ AuditQueryHandler (filter by actor, entity, date range)

API:
  ✗ GET /api/v1/admin/audit-log

Infrastructure:
  ✗ Liquibase migrations (partitioned table)
```

### 3.5 Setting Module
**What exists:** `GlobalSetting`, `TenantSetting`, basic repos, `SettingService` (stub)

**What is MISSING:**
```
Domain:
  ✗ SettingCategory enum
  ✗ isSecret field (mask passwords in API responses)
  ✗ dataType enum (STRING, INTEGER, BOOLEAN, JSON)

Application:
  ✗ UpdateSettingHandler
  ✗ GetSettingsHandler (with category filter)
  ✗ QuotaEnforcer service
  ✗ ResetToDefaultHandler

API:
  ✗ GET  /api/v1/admin/settings
  ✗ PUT  /api/v1/admin/settings/{key}
  ✗ GET  /api/v1/tenants/me/settings
  ✗ PUT  /api/v1/tenants/me/settings/{key}

Infrastructure:
  ✗ Liquibase migrations
```

---

## 4. Liquibase Migration Strategy

All migrations live in:
```
iecommerce-app/src/main/resources/db/changelog/
  master-changelog.xml
  migrations/
    V001__auth_schema.xml          ← (likely exists)
    V002__catalog_schema.xml       ← TO CREATE (main tables)
    V002b__catalog_translations.xml← TO CREATE (all _translations tables)
    V003__customer_schema.xml      ← TO CREATE
    V004__staff_schema.xml         ← TO CREATE
    V005__audit_schema.xml         ← TO CREATE
    V006__setting_schema.xml       ← TO CREATE
```

### Key Design Decisions for Migrations

| Table Group | Key Constraint |
|---|---|
| All tables | `id BIGSERIAL PK`, `created_at`, `updated_at`, `deleted BOOLEAN DEFAULT FALSE`, `tenant_id VARCHAR` |
| `catalog_products` | `UNIQUE(tenant_id, slug)` — slug collision-safe per tenant |
| `catalog_product_translations` | `UNIQUE(product_id, locale)` — one row per locale per product |
| `catalog_category_translations` | `UNIQUE(category_id, locale)` |
| `catalog_facet_translations` | `UNIQUE(facet_id, locale)` |
| `catalog_facet_value_translations` | `UNIQUE(facet_value_id, locale)` |
| `audit_events` | Range-partitioned by year on `created_at` |
| `tenant_settings` | `UNIQUE(tenant_id, key)` |

---

## 5. Translation Design (Cross-Cutting Concern — Catalog)

The catalog module uses a **dedicated translation table per entity** pattern.
All human-readable text that varies by language is stored separately.

### Which columns live where

| Entity | Main Table (invariant) | Translation Table (locale-specific) |
|---|---|---|
| `Product` | `slug`, `status`, `product_type`, `base_price`, `tax_category`, `tags` | `name`, `description`, `short_description`, `meta_title`, `meta_description` |
| `ProductVariant` | `sku`, `price`, `stock_level`, `weight`, `enabled` | `name` |
| `Category` | `slug`, `parent_id`, `materialized_path`, `depth`, `image_url` | `name`, `description` |
| `Collection` | `slug`, `is_automatic`, `rule` | `name`, `description` |
| `Facet` | `code`, `is_filterable` | `name` |
| `FacetValue` | `code` | `value` |

### Locale format
IETF BCP 47 tag stored as `VARCHAR(10)`: `en`, `km`, `zh`, `th`, `vi`, `fr`, `ja`, `ko`

### API contract
- **Write:** `translations` map in request body: `{ "en": { "name": "...", "description": "..." }, "km": { ... } }`
- **Read:** `?locale=km` returns the km translation (or `en` fallback if km not available)
- **Admin full view:** `GET /products/{id}/translations` returns ALL locales as a map

### Business rules
- At least one translation required on CREATE
- `en` is always required as the platform baseline
- `code` on `Facet`/`FacetValue` is locale-invariant — used in URLs and API filters
- `ON DELETE CASCADE` on all translation tables — no orphans possible

---

## 6. Cross-Module Event Contracts

These Spring Modulith internal events must be defined and respected:

| Publisher | Event | Subscriber |
|---|---|---|
| `auth` | `UserRegisteredEvent` | `customer` (auto-create profile) |
| `auth` | `StaffAccountCreatedEvent` | `audit` |
| `staff` | `StaffSuspendedEvent` | `audit` |
| `catalog` | `ProductCreatedEvent` | `audit` |
| `catalog` | `ProductPublishedEvent` | `audit`, `inventory` (create stock record) |
| `catalog` | `ProductArchivedEvent` | `audit`, `inventory` (deactivate stock) |
| `customer` | `CustomerCreatedEvent` | `audit` |
| `order` (future) | `OrderCompletedEvent` | `customer` (loyalty), `audit` |

---

## 7. Domain Model Standards (Applies to All Modules)

All new entities MUST follow:

```java
// ✅ Extend BaseTenantEntity (provides id, createdAt, updatedAt, deleted, tenantId)
public class Product extends BaseTenantEntity { ... }

// ✅ Translation entities use (entityId, locale) as natural key
public class ProductTranslation {
  // locale is immutable identity — no setter
  public ProductTranslation(Product product, String locale, String name) { ... }
  public void update(String name, String description, ...) { ... } // update only text fields
}

// ✅ Enums for status — never raw Strings
public enum ProductStatus { DRAFT, ACTIVE, ARCHIVED }
public enum ProductType  { PHYSICAL, DIGITAL, SERVICE }

// ✅ Named domain methods — no raw setters for state changes
product.publish();          // not product.setStatus(ACTIVE)
product.archive();          // not product.setStatus(ARCHIVED)
product.upsertTranslation("km", "ស្មាតហ្វូន", "...");

// ✅ No @Data (Lombok) — explicit getters only
// ✅ package-info.java in every package
// ✅ No wildcard imports
```

---

## 8. API Design Conventions

All new APIs must follow:

| Concern | Convention |
|---|---|
| Admin APIs | `/api/v1/admin/{resource}` |
| Tenant-self APIs | `/api/v1/tenants/me/{resource}` |
| Public read APIs | `/api/v1/{resource}` |
| Pagination | `?page=0&size=20&sort=createdAt,desc` |
| Locale selection | `?locale=km` (query param, not header) |
| Response shape | `{ data: T, page: {...} }` for lists |
| Error shape | `{ status, error, message, timestamp, path, correlationId }` |
| Documentation | `@Tag`, `@Operation`, `@ApiResponse` on every endpoint |

---

## 9. Spec Documents

| Spec | Status | Description |
|---|---|---|
| [`catalog-manage-spec.md`](catalog-manage-spec.md) | ✅ Done | Full catalog admin API, domain, DB schema, translations |
| `catalog-storefront-spec.md` | ⏳ Planned | Public read API, search, filtering |
| `customer-spec.md` | ⏳ Planned | Customer profile, addresses, loyalty |
| `staff-spec.md` | ⏳ Planned | Staff lifecycle, roles, status |
| `audit-spec.md` | ⏳ Planned | Event log, partitioning, admin API |
| `setting-spec.md` | ⏳ Planned | Settings CRUD, quota enforcement |

---

## 10. Implementation Order (Recommended)

```
Week 1:  Migrations (all 6 modules, including translation tables)
         → Setting API → Audit listener wiring

Week 2:  Catalog domain (with translations) + application layer + API
         → most complex, highest dependency from other modules

Week 3:  Customer enrichment + Staff enrichment

Week 4:  Audit API + integration tests for cross-module events
```

> **Next immediate action:** Start `V002__catalog_schema.xml` + domain entities per [`catalog-manage-spec.md`](catalog-manage-spec.md)
