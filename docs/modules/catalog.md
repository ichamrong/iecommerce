# Module Specification: Catalog

## 1. Purpose
The Catalog module manages the product life cycle, including categorization, variants, attributes, and digital assets. It provides a read-optimized API for discovery and a management API for administration.

## 2. Core Domain Models
- **Product**: The core offering that a merchant sells over the API.
  - *E-commerce*: A physical item (e.g., "T-Shirt").
  - *Booking/Service*: A time-based service (e.g., "Massage", "Consultation").
  - *Accommodation*: A room type (e.g., "Standard Queen").
  - *Universal Attributes*: Name, Description, Base Price, Tax Category.
  - *Logic*: All specific configurations (Variants) under this product share these "Product-level" rules.
- **ProductVariant**: The **Specific Configuration** of a Product.
  - *E-commerce*: Specific SKU (e.g., "T-Shirt - Red/Large").
  - *Accommodation*: Specific offering (e.g., "Standard Queen - Room Only" vs "Standard Queen + Spa Package").
  - *Pricing*: Each variant has its own price/rate.
  - *Asset Linking*: Can have its own specific images.
- **Asset (Media Gallery)**: 
  - **Hierarchy**: Attached to Product or Variant.
  - **Grouping**: Images grouped into "Gallery," "Thumbnail," "Floorplan," etc.
- **Add-on**: A special relationship where one product is offered as an option for another (e.g., "Gift Wrap" for physical goods, or "Breakfast" for Accommodation).
  - *Type*: Can be **Mandatory** (e.g., Cleaning Fee) or **Optional** (e.g., Extra Bed).
- **ProductOption**: Dimensions like "Color/Size" (Physical) or "View/Floor" (Booking).
- **ProductOptionValue**: Specific values like "Red", "Blue", "XL", "Ocean View".
- **Facet & FacetValue**: Dynamic categories for filtering (e.g., "Brand: Apple", "Material: Iron", "Style: Vintage").
  - **Extensible**: New facets can be added via the Admin API without any code changes.
- **ProductAttribute**: Technical specifications that don't vary (e.g., "Screen Size: 6.7 inches").
- **Category**: Hierarchical organization using **Adjacency List + Materialized Path**.
- **Collection**: Dynamic, non-hierarchical groups (e.g., "Valentine's Day", "Summer Essentials").
- **Asset**: Centralized media (Images, Videos, PDFs) storage linked to products and variants.
- **ProductRelationship**: Links between products for **Upsells**, **Cross-sells**, and **Related Products**.

## 3. Architecture: Search & Performance
We apply a strict separation between **Discovery** and **Detail Retrieval**:

### A. Discovery (Full-Text Search)
- Used for **Listing, Filtering, and Suggestions**.
- Powered by **Elasticsearch** or **OpenSearch**.
- Optimized for speed and "fuzzy" matches, not for sensitive logic.

### B. Detail Retrieval (Secure Source of Truth)
- Used for the **Product Detail Page**.
- **Retrieve from Database**: Ensures 100% data accuracy and security (e.g., private attributes or tenant-specific pricing).
- **Redis Caching**: The full `ProductDetail` object is cached in Redis with a TTL.
- **Cache Invalidation**: Cache is evicted automatically when a product is updated.

## 4. Multi-Tenancy Strategy (SaaS)
Products and Categories are strictly isolated by `tenant_id`. Every shop owner (tenant) manages their own catalog. The Headless API ensures that tenant context is securely extracted from the request (e.g., via API keys or JWTs), preventing merchants from accessing each other's data. Global products (system-provided) are handled via a shared `NULL` tenant ID or a system tenant.

## 5. Key Business Logic
- **Slug Generation**: Automatic URL-friendly slug generation from product names.
- **Stock Tracking**: Communicates with the `inventory` module to show "Out of Stock" labels.
- **Price Calculation**: Integrates with the `promotion` module to show discounted prices ($99 -> $79).

## 6. Public APIs (Internal Modulith)
- `CatalogService.getProductDetail(id)`: Returns full product + variants.
- `CatalogService.listByCollection(slug)`: Lists products in a specific group.
- `CatalogService.getCategoryTree()`: Returns the hierarchical category structure.
