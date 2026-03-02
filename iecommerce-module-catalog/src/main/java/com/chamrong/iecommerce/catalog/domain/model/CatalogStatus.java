package com.chamrong.iecommerce.catalog.domain.model;

/**
 * Lifecycle status of a catalog item.
 *
 * <p>DRAFT → PUBLISHED → ARCHIVED. Soft delete (ARCHIVED) preferred for audit and historical
 * invoice accuracy. Maps to {@link com.chamrong.iecommerce.catalog.domain.ProductStatus} ACTIVE as
 * PUBLISHED.
 */
public enum CatalogStatus {

  /** Created but not visible; editable. */
  DRAFT,

  /** Visible on storefront / bookable. */
  PUBLISHED,

  /** No longer sold; kept for historical references. */
  ARCHIVED
}
