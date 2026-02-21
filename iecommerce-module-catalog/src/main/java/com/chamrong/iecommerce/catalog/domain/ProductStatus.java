package com.chamrong.iecommerce.catalog.domain;

/** Lifecycle state of a {@link Product} from creation through to archival. */
public enum ProductStatus {

  /** Created but not visible to the storefront. Default on creation. */
  DRAFT,

  /** Published and visible on the storefront. */
  ACTIVE,

  /** No longer sold; hidden from storefront but kept for historical records. */
  ARCHIVED
}
