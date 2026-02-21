# Catalog Management Module — Detailed Specification

> **Module:** `iecommerce-module-catalog`  
> **Package root:** `com.chamrong.iecommerce.catalog`  
> **Status:** 🔴 Implementation pending (stub exists)  
> **Last Updated:** 2026-02-21  
> **Author:** Platform Team

---

## 1. Purpose & Scope

The Catalog module is the **source of truth** for all product data on the platform.
It manages the full lifecycle of products — from initial draft through public availability
to archival — within a strict **multi-tenant** boundary.

This spec covers **Catalog Management** (the admin/write side):
- Product CRUD and lifecycle
- Category hierarchy management
- Variant and pricing management
- Collections and discovery attributes
- Cross-module contracts (events published)

> For the **public read/storefront** side (search, filtering, slugs), see `catalog-storefront-spec.md` (future).

---

## 2. Domain Model

### 2.1 Aggregate Structure

```
Product (Aggregate Root)
├── ProductVariant[]         ← child entities (1..N)
├── ProductAttribute[]       ← embedded key-value specs
├── ProductRelationship[]    ← upsells, cross-sells, related
└── Facet references[]       ← tag-style filtering values

Category (Aggregate Root)
├── parent: Category         ← adjacency list
└── materializedPath: String ← e.g. "/1/4/12/" for fast tree queries

Collection (Aggregate Root)
└── ProductCollection[]      ← M:N join to Product
```

---

### 2.2 Core Entities & Fields

#### `Product`
| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto | |
| `tenantId` | `String` | NOT NULL, indexed | Multi-tenancy key |
| `name` | `String` | NOT NULL, max 255 | |
| `slug` | `String` | UNIQUE per tenant | URL-safe, auto-generated from name |
| `description` | `String` | TEXT | Rich text / markdown |
| `shortDescription` | `String` | max 512 | Used for cards/listings |
| `status` | `ProductStatus` | NOT NULL, default DRAFT | Lifecycle state |
| `productType` | `ProductType` | NOT NULL | `PHYSICAL`, `DIGITAL`, `SERVICE` |
| `categoryId` | `Long` | FK, nullable | Leaf-node category assignment |
| `basePrice` | `Money` | embedded | The starting/reference price |
| `taxCategory` | `String` | nullable | e.g. `STANDARD`, `ZERO_RATED` |
| `tags` | `String` | nullable | Comma-separated, for search |
| `metaTitle` | `String` | nullable | SEO override |
| `metaDescription` | `String` | nullable | SEO override |
| `createdAt` | `Instant` | auto | |
| `updatedAt` | `Instant` | auto | |
| `deleted` | `boolean` | default false | Soft delete |

**Lifecycle — `ProductStatus` enum:**
```
DRAFT ──publish()──► ACTIVE ──archive()──► ARCHIVED
                        ▲                      │
                        └──reactivate()─────────┘
```

**Type — `ProductType` enum:**
- `PHYSICAL` — has physical stock, weight, dimensions
- `DIGITAL` — downloadable file or key
- `SERVICE` — intangible (massage, consultation)

---

#### `ProductVariant`

Each variant is a specific **purchasable SKU** of a product.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | |
| `productId` | `Long` | FK → Product | |
| `sku` | `String` | UNIQUE per tenant | Stock Keeping Unit |
| `name` | `String` | NOT NULL | e.g. "Red / XL" |
| `price` | `Money` | embedded | Overrides product basePrice |
| `compareAtPrice` | `Money` | nullable | "Was" price for strikethrough display |
| `weight` | `BigDecimal` | nullable | In grams, for shipping |
| `stockLevel` | `Integer` | default 0 | Synced from inventory module |
| `enabled` | `boolean` | default true | Can be toggled without archiving |
| `sortOrder` | `Integer` | default 0 | Display ordering |

---

#### `ProductAttribute`

Static technical specifications that don't vary across variants.

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | PK |
| `productId` | `Long` | FK |
| `attributeKey` | `String` | e.g. `"Screen Size"` |
| `attributeValue` | `String` | e.g. `"6.7 inches"` |
| `unit` | `String` | nullable, e.g. `"inches"`, `"kg"` |
| `sortOrder` | `Integer` | display order |

---

#### `Category`

Hierarchical product organization using **Adjacency List + Materialized Path**
for efficient tree operations.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK | |
| `tenantId` | `String` | NOT NULL | |
| `name` | `String` | NOT NULL | |
| `slug` | `String` | UNIQUE per tenant | |
| `parentId` | `Long` | FK self, nullable | NULL = root category |
| `materializedPath` | `String` | e.g. `/1/4/12/` | Enables `WHERE path LIKE '/1/%'` |
| `depth` | `Integer` | auto-computed | 0 = root |
| `sortOrder` | `Integer` | default 0 | Sibling ordering |
| `description` | `String` | nullable | |
| `imageUrl` | `String` | nullable | Category banner |
| `isActive` | `boolean` | default true | |

**Category Tree Rules:**
- Max depth: **5 levels**
- A category cannot be moved under its own descendant
- Deleting a category with children requires `reassignChildren: true` flag

---

#### `Collection`

Dynamic, non-hierarchical groups. Example: *"Valentine's Day"*, *"Best Sellers"*.

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | PK |
| `tenantId` | `String` | |
| `name` | `String` | |
| `slug` | `String` | UNIQUE per tenant |
| `description` | `String` | |
| `isAutomatic` | `boolean` | If true: products added by rule engine |
| `rule` | `String` | JSON rule for automatic collections |
| `sortOrder` | `Integer` | |
| `isActive` | `boolean` | |

**Join Table:** `catalog_collection_products(collection_id, product_id, sort_order)`

---

#### `Facet` & `FacetValue`

Dynamic, admin-configurable filtering dimensions.  
Example: Facet = *"Brand"*, Values = `["Apple", "Samsung", "Sony"]`

| Entity | Field | Type | Notes |
|---|---|---|---|
| `Facet` | `id` | `Long` | |
| | `tenantId` | `String` | |
| | `name` | `String` | e.g. "Brand" |
| | `code` | `String` | UNIQUE, machine-readable, e.g. "brand" |
| | `isFilterable` | `boolean` | Show in storefront filter panel |
| `FacetValue` | `id` | `Long` | |
| | `facetId` | `Long` | FK |
| | `value` | `String` | e.g. "Apple" |
| | `code` | `String` | e.g. "apple" |

**Join Table:** `catalog_product_facet_values(product_id, facet_value_id)`

---

#### `ProductRelationship`

Links between products for merchandising intelligence.

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | PK |
| `productId` | `Long` | FK → source product |
| `relatedProductId` | `Long` | FK → target product |
| `type` | `RelationshipType` | `UPSELL`, `CROSS_SELL`, `RELATED`, `BUNDLE` |
| `sortOrder` | `Integer` | display order |

---

## 3. Application Layer — Commands & Queries

### 3.1 Commands (Write Side)

#### Product Commands

| Command | Handler | Permissions |
|---|---|---|
| `CreateProductCommand` | `CreateProductHandler` | `catalog:manage` |
| `UpdateProductCommand` | `UpdateProductHandler` | `catalog:manage` |
| `PublishProductCommand` | `PublishProductHandler` | `catalog:publish` |
| `ArchiveProductCommand` | `ArchiveProductHandler` | `catalog:manage` |
| `ReactivateProductCommand` | `ReactivateProductHandler` | `catalog:publish` |
| `DeleteProductCommand` | `DeleteProductHandler` | `catalog:delete` |
| `AddVariantCommand` | `AddVariantHandler` | `catalog:manage` |
| `UpdateVariantCommand` | `UpdateVariantHandler` | `catalog:manage` |
| `RemoveVariantCommand` | `RemoveVariantHandler` | `catalog:manage` |
| `SetRelationshipsCommand` | `SetRelationshipsHandler` | `catalog:manage` |

**`CreateProductCommand` fields:**
```java
public record CreateProductCommand(
    String name,                    // required
    String description,             // optional
    String shortDescription,        // optional
    ProductType productType,        // required
    Long categoryId,                // optional
    Money basePrice,                // required
    String taxCategory,             // optional, default "STANDARD"
    List<CreateVariantDto> variants // min 1 required
) {}
```

**Business rules for `create`:**
1. Auto-generate `slug` from `name` (lowercase, replace spaces with `-`, strip special chars)
2. If `slug` already exists for this tenant → append `-2`, `-3`, etc.
3. Status starts as `DRAFT` — product is NOT visible to storefront
4. At least ONE variant must be provided
5. Quota check: `SettingService.getQuota("max_products")` → reject if exceeded

#### Category Commands

| Command | Handler | Permission |
|---|---|---|
| `CreateCategoryCommand` | `CreateCategoryHandler` | `catalog:manage` |
| `UpdateCategoryCommand` | `UpdateCategoryHandler` | `catalog:manage` |
| `MoveCategoryCommand` | `MoveCategoryHandler` | `catalog:manage` |
| `DeleteCategoryCommand` | `DeleteCategoryHandler` | `catalog:manage` |

**`MoveCategoryCommand` rules:**
1. Cannot move a category to one of its own descendants
2. Must recompute `materializedPath` for the moved category AND all its descendants
3. Published as `CategoryMovedEvent` for storefront cache invalidation

#### Collection Commands

| Command | Handler |
|---|---|
| `CreateCollectionCommand` | `CreateCollectionHandler` |
| `UpdateCollectionCommand` | `UpdateCollectionHandler` |
| `AddProductToCollectionCommand` | `AddProductToCollectionHandler` |
| `RemoveProductFromCollectionCommand` | `RemoveProductFromCollectionHandler` |

---

### 3.2 Queries (Read Side)

| Query Handler | Method | Notes |
|---|---|---|
| `ProductQueryHandler` | `findById(Long id)` | Returns `ProductDetailResponse` |
| | `findBySlug(String slug)` | Storefront lookup |
| | `listByTenant(Pageable)` | Admin list with filters |
| | `listByCategory(Long catId, Pageable)` | Includes subcategories |
| | `listByCollection(String slug, Pageable)` | |
| | `search(String keyword, Pageable)` | Simple DB LIKE search initially |
| `CategoryQueryHandler` | `getTree()` | Full hierarchical tree |
| | `getBreadcrumb(Long id)` | Path from root to given category |
| | `listRoots()` | Top-level categories only |
| `CollectionQueryHandler` | `listCollections(Pageable)` | Admin list |
| | `findBySlug(String slug)` | |

---

### 3.3 DTOs

#### `ProductSummaryResponse` (for list views)
```java
public record ProductSummaryResponse(
    Long id,
    String name,
    String slug,
    String shortDescription,
    ProductStatus status,
    ProductType productType,
    MoneyDto basePrice,
    Integer variantCount,
    String categoryName,
    Instant createdAt
) {}
```

#### `ProductDetailResponse` (for detail / edit views)
```java
public record ProductDetailResponse(
    Long id,
    String name,
    String slug,
    String description,
    String shortDescription,
    ProductStatus status,
    ProductType productType,
    MoneyDto basePrice,
    String taxCategory,
    Long categoryId,
    String categoryName,
    List<VariantResponse> variants,
    List<AttributeResponse> attributes,
    List<FacetValueResponse> facetValues,
    List<RelationshipResponse> relationships,
    String metaTitle,
    String metaDescription,
    String tags,
    Instant createdAt,
    Instant updatedAt
) {}
```

#### `CategoryTreeNode` (recursive)
```java
public record CategoryTreeNode(
    Long id,
    String name,
    String slug,
    String imageUrl,
    Integer depth,
    Integer productCount,
    List<CategoryTreeNode> children
) {}
```

---

## 4. REST API Endpoints

### 4.1 Admin — Product Management
> Base path: `/api/v1/admin/products`  
> Required permission: `catalog:manage` (unless noted)

| Method | Path | Description | Body / Params |
|---|---|---|---|
| `GET` | `/api/v1/admin/products` | List products (paginated) | `?status=&categoryId=&q=&page=&size=` |
| `POST` | `/api/v1/admin/products` | Create new product | `CreateProductCommand` |
| `GET` | `/api/v1/admin/products/{id}` | Get full product detail | — |
| `PUT` | `/api/v1/admin/products/{id}` | Update product | `UpdateProductCommand` |
| `DELETE` | `/api/v1/admin/products/{id}` | Soft-delete product | — |
| `PATCH` | `/api/v1/admin/products/{id}/publish` | Publish (DRAFT→ACTIVE) | — |
| `PATCH` | `/api/v1/admin/products/{id}/archive` | Archive | — |
| `PATCH` | `/api/v1/admin/products/{id}/reactivate` | Reactivate (ARCHIVED→ACTIVE) | — |
| `POST` | `/api/v1/admin/products/{id}/variants` | Add variant | `AddVariantCommand` |
| `PUT` | `/api/v1/admin/products/{id}/variants/{variantId}` | Update variant | `UpdateVariantCommand` |
| `DELETE` | `/api/v1/admin/products/{id}/variants/{variantId}` | Remove variant | — |
| `PUT` | `/api/v1/admin/products/{id}/relationships` | Set relationships (full replace) | `List<RelationshipDto>` |
| `PUT` | `/api/v1/admin/products/{id}/facets` | Assign facet values | `List<Long> facetValueIds` |

### 4.2 Admin — Category Management
> Base path: `/api/v1/admin/categories`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/admin/categories/tree` | Full category tree |
| `GET` | `/api/v1/admin/categories` | Flat list (paginated) |
| `POST` | `/api/v1/admin/categories` | Create category |
| `PUT` | `/api/v1/admin/categories/{id}` | Update category |
| `PATCH` | `/api/v1/admin/categories/{id}/move` | Move to new parent |
| `DELETE` | `/api/v1/admin/categories/{id}` | Delete (with reassignChildren param) |

### 4.3 Admin — Collection Management
> Base path: `/api/v1/admin/collections`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/admin/collections` | List collections |
| `POST` | `/api/v1/admin/collections` | Create collection |
| `PUT` | `/api/v1/admin/collections/{id}` | Update |
| `POST` | `/api/v1/admin/collections/{id}/products` | Add product(s) |
| `DELETE` | `/api/v1/admin/collections/{id}/products/{productId}` | Remove product |

### 4.4 Admin — Facet Management
> Base path: `/api/v1/admin/facets`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/admin/facets` | List all facets with values |
| `POST` | `/api/v1/admin/facets` | Create facet |
| `POST` | `/api/v1/admin/facets/{id}/values` | Add facet value |
| `DELETE` | `/api/v1/admin/facets/{id}/values/{valueId}` | Remove facet value |

---

## 5. Database Schema (Liquibase Migration: `V002__catalog_schema.xml`)

### Tables

```sql
-- Products
CREATE TABLE catalog_products (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(100)  NOT NULL,
    name            VARCHAR(255)  NOT NULL,
    slug            VARCHAR(255)  NOT NULL,
    description     TEXT,
    short_description VARCHAR(512),
    status          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    product_type    VARCHAR(20)   NOT NULL DEFAULT 'PHYSICAL',
    category_id     BIGINT        REFERENCES catalog_categories(id),
    base_price_amount   NUMERIC(19,4),
    base_price_currency VARCHAR(3),
    tax_category    VARCHAR(50)   DEFAULT 'STANDARD',
    tags            TEXT,
    meta_title      VARCHAR(255),
    meta_description VARCHAR(512),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    UNIQUE (tenant_id, slug)
);

-- Variants
CREATE TABLE catalog_product_variants (
    id                      BIGSERIAL PRIMARY KEY,
    product_id              BIGINT        NOT NULL REFERENCES catalog_products(id),
    sku                     VARCHAR(100)  NOT NULL,
    name                    VARCHAR(255)  NOT NULL,
    price_amount            NUMERIC(19,4),
    price_currency          VARCHAR(3),
    compare_at_price_amount NUMERIC(19,4),
    compare_at_price_currency VARCHAR(3),
    weight                  NUMERIC(10,3),
    stock_level             INTEGER       NOT NULL DEFAULT 0,
    enabled                 BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order              INTEGER       NOT NULL DEFAULT 0,
    deleted                 BOOLEAN       NOT NULL DEFAULT FALSE,
    UNIQUE (sku)
);

-- Categories (Adjacency List + Materialized Path)
CREATE TABLE catalog_categories (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         VARCHAR(100)  NOT NULL,
    name              VARCHAR(255)  NOT NULL,
    slug              VARCHAR(255)  NOT NULL,
    parent_id         BIGINT        REFERENCES catalog_categories(id),
    materialized_path VARCHAR(1000), -- e.g. /1/4/12/
    depth             INTEGER       NOT NULL DEFAULT 0,
    sort_order        INTEGER       NOT NULL DEFAULT 0,
    description       TEXT,
    image_url         VARCHAR(500),
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted           BOOLEAN       NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, slug)
);

-- Collections
CREATE TABLE catalog_collections (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    VARCHAR(100) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    slug         VARCHAR(255) NOT NULL,
    description  TEXT,
    is_automatic BOOLEAN      NOT NULL DEFAULT FALSE,
    rule         JSONB,
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, slug)
);

-- Collection ↔ Product (M:N)
CREATE TABLE catalog_collection_products (
    collection_id BIGINT NOT NULL REFERENCES catalog_collections(id),
    product_id    BIGINT NOT NULL REFERENCES catalog_products(id),
    sort_order    INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (collection_id, product_id)
);

-- Facets
CREATE TABLE catalog_facets (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    VARCHAR(100)  NOT NULL,
    name         VARCHAR(255)  NOT NULL,
    code         VARCHAR(100)  NOT NULL,
    is_filterable BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (tenant_id, code)
);

-- Facet Values
CREATE TABLE catalog_facet_values (
    id        BIGSERIAL PRIMARY KEY,
    facet_id  BIGINT       NOT NULL REFERENCES catalog_facets(id),
    value     VARCHAR(255) NOT NULL,
    code      VARCHAR(100) NOT NULL,
    UNIQUE (facet_id, code)
);

-- Product ↔ FacetValue (M:N)
CREATE TABLE catalog_product_facet_values (
    product_id     BIGINT NOT NULL REFERENCES catalog_products(id),
    facet_value_id BIGINT NOT NULL REFERENCES catalog_facet_values(id),
    PRIMARY KEY (product_id, facet_value_id)
);

-- Product Attributes
CREATE TABLE catalog_product_attributes (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT       NOT NULL REFERENCES catalog_products(id),
    attribute_key   VARCHAR(255) NOT NULL,
    attribute_value VARCHAR(500) NOT NULL,
    unit            VARCHAR(50),
    sort_order      INTEGER NOT NULL DEFAULT 0
);

-- Product Relationships
CREATE TABLE catalog_product_relationships (
    id                BIGSERIAL PRIMARY KEY,
    product_id        BIGINT      NOT NULL REFERENCES catalog_products(id),
    related_product_id BIGINT     NOT NULL REFERENCES catalog_products(id),
    type              VARCHAR(20) NOT NULL,  -- UPSELL, CROSS_SELL, RELATED, BUNDLE
    sort_order        INTEGER NOT NULL DEFAULT 0,
    UNIQUE (product_id, related_product_id, type)
);
```

### Indexes

```sql
-- Hot-path indexes
CREATE INDEX idx_products_tenant_status   ON catalog_products(tenant_id, status) WHERE deleted = FALSE;
CREATE INDEX idx_products_category        ON catalog_products(category_id)       WHERE deleted = FALSE;
CREATE INDEX idx_products_slug            ON catalog_products(tenant_id, slug)    WHERE deleted = FALSE;
CREATE INDEX idx_variants_product         ON catalog_product_variants(product_id) WHERE deleted = FALSE;
CREATE INDEX idx_categories_tenant_path   ON catalog_categories(tenant_id, materialized_path);
-- GIN for full-text search on name + description
CREATE INDEX idx_products_fts ON catalog_products
    USING GIN (to_tsvector('english', name || ' ' || COALESCE(description, '')));
```

---

## 6. Events Published

All events implement `com.chamrong.iecommerce.common.DomainEvent`.

| Event | Trigger | Subscribers |
|---|---|---|
| `ProductCreatedEvent` | Product saved in DRAFT | `audit` |
| `ProductPublishedEvent` | Status → ACTIVE | `audit`, `inventory` (create stock record) |
| `ProductArchivedEvent` | Status → ARCHIVED | `audit`, `inventory` (deactivate stock) |
| `ProductUpdatedEvent` | Any field update | `audit` |
| `ProductDeletedEvent` | Soft-delete | `audit` |
| `CategoryMovedEvent` | Category parent changed | `audit` (storefront cache in future) |

**`ProductPublishedEvent` payload:**
```java
public record ProductPublishedEvent(
    Long productId,
    String tenantId,
    String productName,
    List<Long> variantIds,
    Instant publishedAt
) implements DomainEvent {}
```

---

## 7. Business Rules Summary

| Rule | Detail |
|---|---|
| **Slug uniqueness** | Per tenant, auto-generated, collision-safe with numeric suffix |
| **Draft-first** | New products always start as `DRAFT` — never auto-published |
| **Publish gate** | Product must have ≥ 1 enabled variant and a name to be published |
| **Soft delete only** | `DELETE` endpoint sets `deleted=true`, never removes DB rows |
| **Quota enforcement** | `SettingService.getQuota("max_products")` checked before CREATE |
| **SKU uniqueness** | SKU is unique across the entire tenant (not just per product) |
| **Category depth** | Maximum 5 levels deep |
| **Category move** | Cannot set a category's parent to one of its own descendants |
| **Immutable IDs** | IDs are never reassigned or reused |
| **Tenant isolation** | All queries MUST include `tenant_id` filter — no cross-tenant reads |

---

## 8. Permission Reference

| Permission | Description |
|---|---|
| `catalog:manage` | Create, update, and delete products, categories, collections |
| `catalog:publish` | Move products from DRAFT → ACTIVE or ARCHIVED → ACTIVE |
| `catalog:delete` | Soft-delete products (separate from manage) |
| `catalog:read` | Read-only access to admin catalog (for reporting roles) |

---

## 9. Package Structure (Target)

```
iecommerce-module-catalog/src/main/java/com/chamrong/iecommerce/catalog/
│
├── package-info.java                          ← module-level Javadoc
├── CatalogApi.java                            ← public interface for inter-module calls
│
├── domain/
│   ├── package-info.java
│   ├── Product.java                           ← Aggregate Root
│   ├── ProductStatus.java                     ← enum: DRAFT, ACTIVE, ARCHIVED
│   ├── ProductType.java                       ← enum: PHYSICAL, DIGITAL, SERVICE
│   ├── ProductVariant.java
│   ├── ProductAttribute.java
│   ├── ProductRelationship.java
│   ├── RelationshipType.java                  ← enum: UPSELL, CROSS_SELL, RELATED
│   ├── Category.java                          ← Aggregate Root
│   ├── Collection.java                        ← Aggregate Root
│   ├── Facet.java
│   ├── FacetValue.java
│   ├── ProductRepository.java                 ← port (interface)
│   ├── CategoryRepository.java
│   ├── CollectionRepository.java
│   └── FacetRepository.java
│
├── application/
│   ├── package-info.java
│   ├── command/
│   │   ├── CreateProductCommand.java
│   │   ├── CreateProductHandler.java
│   │   ├── UpdateProductCommand.java
│   │   ├── UpdateProductHandler.java
│   │   ├── PublishProductHandler.java
│   │   ├── ArchiveProductHandler.java
│   │   ├── DeleteProductHandler.java
│   │   ├── AddVariantCommand.java
│   │   ├── AddVariantHandler.java
│   │   ├── UpdateVariantCommand.java
│   │   ├── UpdateVariantHandler.java
│   │   ├── CreateCategoryCommand.java
│   │   ├── CreateCategoryHandler.java
│   │   ├── MoveCategoryCommand.java
│   │   └── MoveCategoryHandler.java
│   ├── query/
│   │   ├── ProductQueryHandler.java
│   │   └── CategoryQueryHandler.java
│   └── dto/
│       ├── ProductSummaryResponse.java
│       ├── ProductDetailResponse.java
│       ├── VariantResponse.java
│       ├── AttributeResponse.java
│       ├── CategoryTreeNode.java
│       └── MoneyDto.java
│
├── api/
│   ├── package-info.java
│   ├── ProductController.java                 ← /api/v1/admin/products
│   └── CategoryController.java                ← /api/v1/admin/categories
│
└── infrastructure/
    ├── package-info.java
    └── persistence/
        ├── JpaProductRepository.java
        ├── JpaCategoryRepository.java
        ├── JpaCollectionRepository.java
        └── JpaFacetRepository.java
```

---

## 10. Open Questions / Decisions Needed

| # | Question | Default decision |
|---|---|---|
| OQ-1 | Should `slug` be editable after publishing? | ❌ No — immutable after first publish (SEO risk) |
| OQ-2 | Per-variant images or only per-product? | Per-product now, per-variant in v2 (needs `asset` module) |
| OQ-3 | Should `stockLevel` live on `ProductVariant` or only in `inventory` module? | Denormalized copy on variant, updated via event from `inventory` |
| OQ-4 | Tax calculation — inline or external service? | `taxCategory` stored, calculation delegated to `order` module |
| OQ-5 | Full-text search — DB `tsvector` or Elasticsearch? | DB GIN index for v1, Elasticsearch as Phase 2 |

---

## 11. Multi-Locale Translation (i18n — DB Layer Only)

> **Scope:** This is a **database schema concern only**.
> No Spring `MessageSource`, no `LocaleResolver` bean, no Thymeleaf i18n.
> The API accepts a `locale` parameter and reads from the appropriate translation row.

---

### 11.1 Pattern: Column Migration to Translation Tables

Translatable text columns are **removed from the main table** and moved into a
dedicated `<entity>_translations` table keyed by `(entity_id, locale)`.

**Locale format:** IETF BCP 47 tag stored as `VARCHAR(10)` — e.g. `en`, `km`, `zh`, `th`, `vi`

---

### 11.2 Column Migration per Entity

#### `catalog_products` — columns that MOVE out

| Column | Moves to |
|---|---|
| `name` | `catalog_product_translations` |
| `description` | `catalog_product_translations` |
| `short_description` | `catalog_product_translations` |
| `meta_title` | `catalog_product_translations` |
| `meta_description` | `catalog_product_translations` |

**Stays on main table** (locale-invariant):
`slug`, `status`, `product_type`, `base_price_*`, `tax_category`, `tags`, `category_id`

---

#### `catalog_categories` — columns that MOVE out

| Column | Moves to |
|---|---|
| `name` | `catalog_category_translations` |
| `description` | `catalog_category_translations` |

**Stays on main table**: `slug`, `parent_id`, `materialized_path`, `depth`, `sort_order`, `image_url`, `is_active`

---

#### `catalog_collections` — columns that MOVE out

| Column | Moves to |
|---|---|
| `name` | `catalog_collection_translations` |
| `description` | `catalog_collection_translations` |

---

#### `catalog_facets` — columns that MOVE out

| Column | Moves to |
|---|---|
| `name` | `catalog_facet_translations` |

**Stays on main table**: `code`, `is_filterable` (machine-readable identifiers — locale-invariant)

---

#### `catalog_facet_values` — columns that MOVE out

| Column | Moves to |
|---|---|
| `value` | `catalog_facet_value_translations` |

**Stays on main table**: `code` (e.g. `"apple"` — locale-invariant key used in filters/URLs)

---

#### `catalog_product_variants` — columns that MOVE out

| Column | Moves to |
|---|---|
| `name` | `catalog_product_variant_translations` |

**Stays on main table**: `sku`, `price_*`, `stock_level`, `weight`, `enabled`, `sort_order`

---

### 11.3 Translation Table Schemas

```sql
-- Product translations
CREATE TABLE catalog_product_translations (
    id                BIGSERIAL    PRIMARY KEY,
    product_id        BIGINT       NOT NULL REFERENCES catalog_products(id) ON DELETE CASCADE,
    locale            VARCHAR(10)  NOT NULL,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    short_description VARCHAR(512),
    meta_title        VARCHAR(255),
    meta_description  VARCHAR(512),
    UNIQUE (product_id, locale)
);

-- Category translations
CREATE TABLE catalog_category_translations (
    id          BIGSERIAL    PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES catalog_categories(id) ON DELETE CASCADE,
    locale      VARCHAR(10)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    UNIQUE (category_id, locale)
);

-- Collection translations
CREATE TABLE catalog_collection_translations (
    id            BIGSERIAL    PRIMARY KEY,
    collection_id BIGINT       NOT NULL REFERENCES catalog_collections(id) ON DELETE CASCADE,
    locale        VARCHAR(10)  NOT NULL,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    UNIQUE (collection_id, locale)
);

-- Facet translations  (facet label: "Brand" → "ម៉ាក" in km)
CREATE TABLE catalog_facet_translations (
    id       BIGSERIAL    PRIMARY KEY,
    facet_id BIGINT       NOT NULL REFERENCES catalog_facets(id) ON DELETE CASCADE,
    locale   VARCHAR(10)  NOT NULL,
    name     VARCHAR(255) NOT NULL,
    UNIQUE (facet_id, locale)
);

-- Facet value translations  ("Apple" → "អាប់ផ្លោ" in km)
CREATE TABLE catalog_facet_value_translations (
    id             BIGSERIAL    PRIMARY KEY,
    facet_value_id BIGINT       NOT NULL REFERENCES catalog_facet_values(id) ON DELETE CASCADE,
    locale         VARCHAR(10)  NOT NULL,
    value          VARCHAR(255) NOT NULL,
    UNIQUE (facet_value_id, locale)
);

-- Variant translations  ("Red / XL" → "ក្រហម / XL" in km)
CREATE TABLE catalog_product_variant_translations (
    id         BIGSERIAL    PRIMARY KEY,
    variant_id BIGINT       NOT NULL REFERENCES catalog_product_variants(id) ON DELETE CASCADE,
    locale     VARCHAR(10)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    UNIQUE (variant_id, locale)
);
```

**Indexes:**
```sql
CREATE INDEX idx_product_trans_locale   ON catalog_product_translations(product_id, locale);
CREATE INDEX idx_category_trans_locale  ON catalog_category_translations(category_id, locale);
CREATE INDEX idx_facet_trans_locale     ON catalog_facet_translations(facet_id, locale);
CREATE INDEX idx_fv_trans_locale        ON catalog_facet_value_translations(facet_value_id, locale);
```

---

### 11.4 Java Entity Design

```java
// catalog/domain/ProductTranslation.java
@Entity
@Table(name = "catalog_product_translations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "locale"}))
public class ProductTranslation {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false, length = 10)
  private String locale;           // "en", "km", "zh"

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 512)
  private String shortDescription;

  @Column(length = 255)
  private String metaTitle;

  @Column(length = 512)
  private String metaDescription;

  // Package-private constructor for JPA
  protected ProductTranslation() {}

  public ProductTranslation(Product product, String locale, String name) {
    this.product = product;
    this.locale  = locale;
    this.name    = name;
  }

  /** Update mutable text fields — locale is identity, cannot be changed. */
  public void update(String name, String description, String shortDescription,
                     String metaTitle, String metaDescription) {
    this.name             = name;
    this.description      = description;
    this.shortDescription = shortDescription;
    this.metaTitle        = metaTitle;
    this.metaDescription  = metaDescription;
  }

  // Getters only — no public setters
  public String getLocale()            { return locale; }
  public String getName()              { return name; }
  public String getDescription()       { return description; }
  public String getShortDescription()  { return shortDescription; }
  public String getMetaTitle()         { return metaTitle; }
  public String getMetaDescription()   { return metaDescription; }
}
```

**`Product` aggregate — translation management:**
```java
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProductTranslation> translations = new ArrayList<>();

/** Insert or update a translation for the given locale. */
public void upsertTranslation(String locale, String name, String description,
                               String shortDescription, String metaTitle, String metaDescription) {
  translations.stream()
      .filter(t -> t.getLocale().equals(locale))
      .findFirst()
      .ifPresentOrElse(
          t -> t.update(name, description, shortDescription, metaTitle, metaDescription),
          () -> translations.add(
              new ProductTranslation(this, locale, name)
                  .withDetails(description, shortDescription, metaTitle, metaDescription))
      );
}

/** Get translation for a specific locale — empty if not provided. */
public Optional<ProductTranslation> translationFor(String locale) {
  return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
}
```

---

### 11.5 API Contract (locale as a data field)

Translations are managed explicitly via the API — no automatic header detection needed.

#### Create / Update — `translations` map in request body

```json
POST /api/v1/admin/products
{
  "slug": "samsung-galaxy-s25",
  "productType": "PHYSICAL",
  "basePrice": { "amount": 999.00, "currency": "USD" },
  "categoryId": 5,
  "translations": {
    "en": {
      "name": "Samsung Galaxy S25",
      "description": "The next generation flagship.",
      "shortDescription": "Snapdragon 8 Elite. 50MP camera.",
      "metaTitle": "Buy Samsung Galaxy S25",
      "metaDescription": "Best price on Samsung Galaxy S25 online."
    },
    "km": {
      "name": "សាំស៊ុង ហ្គាឡាក់ស៊ី S25",
      "description": "ស្មាតហ្វូនជំនាន់ថ្មី",
      "shortDescription": "ស្នេបដ្រាហ្គន 8 អេលីត",
      "metaTitle": null,
      "metaDescription": null
    }
  },
  "variants": [...]
}
```

#### Read — caller specifies `locale` query param

```
GET /api/v1/admin/products/42?locale=km
→ Returns product with km translation (name, description in Khmer)

GET /api/v1/admin/products/42/translations
→ Returns ALL translations as a map: { "en": {...}, "km": {...} }
```

#### Translation-only endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/admin/products/{id}/translations` | All locale entries for a product |
| `PUT` | `/api/v1/admin/products/{id}/translations/{locale}` | Upsert one locale |
| `DELETE` | `/api/v1/admin/products/{id}/translations/{locale}` | Remove a locale entry |
| `GET` | `/api/v1/admin/facets/{id}/translations` | All locale entries for a facet label |
| `PUT` | `/api/v1/admin/facets/{id}/translations/{locale}` | Upsert facet label translation |
| `GET` | `/api/v1/admin/facets/values/{id}/translations` | All locale entries for a facet value |
| `PUT` | `/api/v1/admin/facets/values/{id}/translations/{locale}` | Upsert facet value translation |

---

### 11.6 Business Rules

| Rule | Detail |
|---|---|
| **Min 1 translation** | Every product/category/facet must have at least one translation on CREATE |
| **`en` required** | English (`en`) translation is always required as the platform baseline |
| **`code` stays on main table** | `facets.code` and `facet_values.code` are locale-invariant machine identifiers |
| **`slug` stays on main table** | Product slug is locale-invariant (URL-based on `en` name) |
| **No cascade delete restriction** | Translations are deleted automatically via `ON DELETE CASCADE` |
| **Upsert semantics** | `PUT /translations/{locale}` creates if missing, updates if exists |

