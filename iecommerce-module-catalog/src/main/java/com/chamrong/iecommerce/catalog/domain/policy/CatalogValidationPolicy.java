package com.chamrong.iecommerce.catalog.domain.policy;

/**
 * Encapsulates validation rules for catalog items (required fields, uniqueness, consistency).
 *
 * <p>Used before create/update/publish. Examples: ROOM_UNIT must reference ROOM_TYPE;
 * POS_ITEM should have SKU or barcode; slug format and uniqueness.
 */
public final class CatalogValidationPolicy {

  private CatalogValidationPolicy() {}

  /** Validates that a slug is non-blank and URL-safe. */
  public static void validateSlug(String slug) {
    if (slug == null || slug.isBlank()) {
      throw new IllegalArgumentException("Slug is required");
    }
    if (!slug.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$|^[a-z0-9]$")) {
      throw new IllegalArgumentException("Slug must be lowercase alphanumeric and hyphens only");
    }
  }

  /** Validates that SKU is non-blank when provided. */
  public static void validateSku(String sku) {
    if (sku != null && sku.isBlank()) {
      throw new IllegalArgumentException("SKU cannot be blank when provided");
    }
  }
}
