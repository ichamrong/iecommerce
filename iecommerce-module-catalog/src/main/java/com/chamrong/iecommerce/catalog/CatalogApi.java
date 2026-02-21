package com.chamrong.iecommerce.catalog;

import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Public API of the Catalog module.
 *
 * <p>Other modules (Order, Inventory) MUST only depend on this interface, never on internal classes
 * like ProductQueryHandler or CatalogService. This is the Spring Modulith boundary.
 */
public interface CatalogApi {

  /**
   * Checks whether a product variant exists and is available for purchase.
   *
   * @param variantId the variant ID from the Order line
   * @return variant info if found and ACTIVE
   */
  Optional<ProductVariantInfo> findActiveVariant(Long variantId);

  /** Returns the current stock level of a variant (denormalized copy). */
  int getStockLevel(Long variantId);

  /** Updates the stock level of a variant (called by Inventory module on stock events). */
  void updateStockLevel(Long variantId, int newStockLevel);

  // ── Projection returned to other modules ────────────────────────────────

  /** Minimal read projection — avoids exposing domain entities cross-module. */
  record ProductVariantInfo(
      Long variantId,
      Long productId,
      String sku,
      String productName, // resolved to calling module's preferred locale ("en" default)
      BigDecimal priceAmount,
      String priceCurrency,
      ProductStatus productStatus,
      boolean variantEnabled) {}
}
