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

### Phase 1 — SaaS Foundation & Core Subscriptions
**Goal:** Build the multi-tenant SaaS infrastructure required to onboard Shop Admins and securely bill them.

| # | Task | Module | Why First |
|---|---|---|---|
| 1.1 | Write core Liquibase migrations for multi-tenancy | All | DB schema is the bedrock |
| 1.2 | Implement `Subscription` domain (Plans, Tenant limits) | Subscription | Gatekeeper for all subsequent features |
| 1.3 | Complete Auth/RBAC mapping to Keycloak | Auth | Shop Admins need to log in securely |
| 1.4 | Complete Tenant Settings & Quota Enforcement | Setting | Used by other modules to enforce plan limits |
| 1.5 | Wire `AuditService` listeners | Audit | Must capture events from day one for compliance |
| 1.6 | Enrich Staff Profiles | Staff | Admins managing their own store employees |

### Phase 2 — Accommodation Service APIs
**Goal:** Sell the "Hotel & Rental" API package to hoteliers, ensuring they can define properties and accept nightly bookings.

| # | Task | Module | Notes |
|---|---|---|---|
| 2.1 | Catalog mapping for `ACCOMMODATION` types | Catalog | Room types, features, amenities |
| 2.2 | Time-based calendar domain | Booking | Blackout dates, minimum stays, availability checks |
| 2.3 | Media/Gallery integration | Asset | Minio storage for room/property images |
| 2.4 | Nightly rate calculation engine | Order | Processing check-in/check-out dates into monetary orders |
| 2.5 | Accomodation Order APIs | Order | Handling deposits and cancellations |

### Phase 3 — E-commerce APIs
**Goal:** Sell the "Retail Store" API package to shop owners selling physical or digital goods.

| # | Task | Module | Notes |
|---|---|---|---|
| 3.1 | Catalog mapping for `PHYSICAL` and `DIGITAL` types | Catalog | Variants, SKUs, Translations, Categories |
| 3.2 | Physical Stock management | Inventory | Reserving, tracking, and auditing physical counts |
| 3.3 | Physical Fulfillment workflows | Order | Pick, pack, and ship logic (Tracking numbers) |
| 3.4 | Customer profile enrichment | Customer | Multiple shipping addresses, loyalty points |
| 3.5 | Promotion rules | Promotion | Voucher codes, percentage discounts |

### Phase 4 — Booking Service APIs
**Goal:** Sell the "Appointments" package for time-based services (Massage, Salons, Consultants).

| # | Task | Module | Notes |
|---|---|---|---|
| 4.1 | Catalog mapping for `BOOKING` types | Catalog | Service duration, required staff |
| 4.2 | Service Booking engine | Booking | Hourly/minute slot reservation, Staff scheduling |
| 4.3 | Notification reminders | Notification | Email/WhatsApp reminders 24h before appt |
| 4.4 | Post-service review collection | Review | Verified service, rating system |

### Phase 5 — POS (Point of Sale) APIs
**Goal:** Allow physical retail stores to operate offline-capable cash registers powered by the headless API.

| # | Task | Module | Notes |
|---|---|---|---|
| 5.1 | Terminal and Session tracking | Auth | Identify which cash register initiated the sale |
| 5.2 | Instant Inventory relief | Inventory | Bypass standard "Pick/Pack" for immediate handover |
| 5.3 | Local Receipt generation | Invoice | PDF/Print-ready thermal receipts |
| 5.4 | End-of-day reconciliation reports | Report | Sales per cashier, cash drawer discrepancy |

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
  ✅ ProductType enum (PHYSICAL, DIGITAL, SERVICE, BOOKING, ACCOMMODATION)
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
public enum ProductType  { PHYSICAL, DIGITAL, SERVICE, BOOKING, ACCOMMODATION }

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
Phase 1:  SaaS Foundation (Subscription, Auth, Setting, Staff, Audit)
          → Setup multi-tenancy and the ability for Shop Admins to register and pay.

Phase 2:  Accommodation APIs (Catalog [Room Types], Booking Calendars, Nightly Orders)
          → First monetization target (Hotels/Resorts).

Phase 3:  E-Commerce APIs (Catalog [Physical/Digital], Inventory, Retail Orders)
          → Open platform to retail online shops.

Phase 4:  Booking Service APIs (Catalog [Services], Hourly Bookings, Reviews)
          → Salons, Clinics, Consulting services.

Phase 5:  POS APIs (Terminal tracking, Offline sync, Thermal receipts)
          → Physical brick-and-mortar integration for existing E-com clients.
```

> **Next immediate action:** Start `V002__catalog_schema.xml` + domain entities per [`catalog-manage-spec.md`](catalog-manage-spec.md)
