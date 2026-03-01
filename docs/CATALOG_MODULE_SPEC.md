# Catalog Module — Enterprise Spec

## 1. Overview

The catalog module provides multi-vertical catalog support: e-commerce products, POS items (barcode/SKU), accommodation (room types/units), and services. It follows DDD + Hexagonal structure with cursor-only pagination, tenant isolation, and ASVS-aligned security.

## 2. Package Structure

```
com.chamrong.iecommerce.catalog
├── api                    — REST controllers (Product, Category, Public)
├── application
│   ├── command            — Create/Update/Publish/Archive handlers
│   ├── query              — ProductQueryHandler, CategoryQueryHandler
│   ├── usecase            — Orchestration
│   └── dto                — Request/Response, CatalogFilters
├── domain
│   ├── model              — CatalogItemType, CatalogStatus (pure enums)
│   ├── event              — CatalogItemCreatedEvent, CatalogItemPublishedEvent
│   ├── ports              — ProductRepositoryPort, CategoryRepositoryPort
│   ├── policy             — CatalogValidationPolicy
│   ├── service            — SearchNormalizationService
│   └── exception          — CatalogDomainException
└── infrastructure
    ├── config              — CatalogConfiguration
    ├── persistence         — JPA adapters, Spring Data repos
    ├── persistence.jpa     — (optional) entity-only package
    ├── cache               — CatalogCachePort impl (Redis / NoOp)
    ├── outbox              — (optional) CatalogOutboxEventEntity, relay
    └── client              — (optional) external search adapter
```

- **Domain has no Spring/JPA.** Repository interfaces live in `domain/ports`.
- **List endpoints** use shared `CursorPageResponse` + `CursorCodec` + `FilterHasher` from iecommerce-common. Cursor from a different filter set returns `400 INVALID_CURSOR_FILTER_MISMATCH`.

## 3. Item Types (Multi-Vertical)

| Type                | Use case                          | Notes                                      |
|---------------------|-----------------------------------|--------------------------------------------|
| PRODUCT             | E-commerce goods                  | SKUs, variants, inventory                  |
| POS_ITEM            | Fast lookup by barcode/SKU        | Compatible with sale module                |
| SERVICE             | Add-on services                   | Cleaning, minibar, tour                    |
| ROOM_TYPE           | Accommodation room type           | Rate plan link, capacity                   |
| ROOM_UNIT           | Physical unit (e.g. Room 101)      | Unique code per tenant                     |
| APPOINTMENT_SERVICE  | Slot-based (future)               | Booking module                             |

Current implementation maps to existing `ProductType` (PHYSICAL, DIGITAL, SERVICE, BOOKING, ACCOMMODATION). `CatalogItemType` in `domain.model` aligns with the spec for future expansion.

## 4. Status Lifecycle

- **DRAFT** → editable, not visible on storefront.
- **PUBLISHED** (ACTIVE in DB) → visible, bookable/sellable.
- **ARCHIVED** → soft delete; kept for historical invoices/orders.

Publish requires at least one translation and one enabled variant. Archive does not break existing orders/invoices; references remain valid.

## 5. Search & Listing

- **GET /api/v1/admin/products** — Cursor pagination; filters: `status`, `categoryId`, `keyword`. Uses keyset `(created_at DESC, id DESC)`. `filterHash` binds cursor to filters; mismatch → 400.
- **GET /api/v1/admin/products/{id}** — Tenant-safe; IDOR prevented via `TenantGuard`.
- **GET /api/v1/admin/products/by-sku/{sku}** — Product that has a variant with this SKU (tenant-scoped).
- **GET /api/v1/admin/products/by-barcode/{barcode}** — Product with this barcode (tenant-scoped).
- **GET /api/v1/products** — Public list; only ACTIVE, same cursor contract.

Search: keyword uses GIN full-text on `catalog_product_translations` (name + description). Normalized (trim, lower) where applicable. Avoid leading-wildcard LIKE; prefer prefix or FTS.

## 6. Uniqueness & Indexes

- **Slug:** `UNIQUE (tenant_id, slug)`.
- **Barcode:** `UNIQUE (tenant_id, barcode)` when not null (partial index).
- **Variant SKU:** Unique per variant; product lookup by SKU is tenant-scoped via join to product.
- **Keyset:** `(tenant_id, created_at DESC, id DESC)` for list (existing `idx_products_cursor`).

Liquibase: `changelog-v2-catalog.xml`, `changelog-v16-catalog-hardening.xml`, `changelog-v30-catalog-barcode-sku.xml`.

## 7. Security (ASVS)

- **L1:** Validation at API boundary; tenant scope on every load; IDOR prevention via `TenantGuard.requireSameTenant`.
- **L2-grade (admin mutations):** Optimistic locking (`@Version`); audit of catalog changes (via audit module when integrated); idempotency for create/publish (when implemented).
- **Permissions:** e.g. `CATALOG_READ` / `CATALOG_MANAGE`; separate `PRICE_RULE_MANAGE` if price rules live in catalog.
- **Feature gating:** Accommodation item types (ROOM_TYPE, ROOM_UNIT) can require Accommodation add-on; POS features can require POS add-on.

## 8. Caching

- Cache only **PUBLISHED** (ACTIVE) items for read endpoints (by tenant + itemId or slug).
- Cache SKU/barcode lookups with short TTL for POS speed.
- Invalidate on publish/update/archive (via `CatalogCachePort.evictProduct` / `evictProductBySlug`).
- **Do not** cache cursor pages (stale cursor risk).
- **Do not** cache draft/private items in shared cache.

## 9. Integration Points

- **Inventory:** May react to CatalogItemCreated/Published (stock visibility).
- **Order/Sale:** Reference product/category by id; archival does not break references.
- **Booking:** Room types/units referenced by product id; catalog provides lookup.
- **Outbox:** CatalogItemCreatedEvent, CatalogItemPublishedEvent can be written to outbox and relayed (when outbox is implemented in catalog).

## 10. UAT Scenarios

1. **POS scan flow:** Create product with barcode; GET by-barcode returns product; add to sale.
2. **Accommodation room setup:** Create ROOM_TYPE product; create ROOM_UNIT products with reference to type; list by category.
3. **Publish/archival:** Publish product → visible on storefront; archive → hidden; existing order line items still show product id/snapshot.
4. **Cursor filter mismatch:** List with status=DRAFT; copy nextCursor; change filter to status=ACTIVE; pass cursor → 400 INVALID_CURSOR_FILTER_MISMATCH.
5. **Tenant isolation:** Tenant A cannot read or list Tenant B’s products (enforced by TenantContext/TenantGuard on all reads).

## 11. Assumptions

- **Product** remains the main aggregate in persistence (JPA entity in domain package for now); `CatalogItem` as a pure domain aggregate can be introduced later with a mapper in infrastructure.
- **Categories** use existing `Category` entity and `CategoryRepositoryPort` in `domain/ports`.
- **Price rules** and **AccommodationController** are optional and can be added in a follow-up; schema and ports are prepared.
- **iecommerce-common** provides `CursorCodec`, `FilterHasher`, `CursorPageResponse`, `CursorPayload`, `InvalidCursorException`, `TenantGuard`, `TenantContext`.
- Liquibase changelogs live in **iecommerce-app**; catalog module has no Liquibase dependency.
