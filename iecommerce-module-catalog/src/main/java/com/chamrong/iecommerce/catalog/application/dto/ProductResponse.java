package com.chamrong.iecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.List;

/** Read-side representation of a product, resolved to a specific locale. */
public record ProductResponse(
    Long id,
    String slug,
    String status,
    String productType,
    Long categoryId,
    BigDecimal basePriceAmount,
    String basePriceCurrency,
    BigDecimal compareAtPriceAmount,
    String compareAtPriceCurrency,
    String taxCategory,
    String tags,

    // Resolved translation for the requested locale
    String resolvedLocale,
    String name,
    String description,
    String shortDescription,
    String metaTitle,
    String metaDescription,
    List<VariantResponse> variants) {

  public record VariantResponse(
      Long id,
      String sku,
      BigDecimal priceAmount,
      String priceCurrency,
      BigDecimal compareAtPriceAmount,
      String compareAtPriceCurrency,
      BigDecimal weightGrams,
      int stockLevel,
      boolean enabled,
      int sortOrder,
      String name // resolved variant name for locale
      ) {}
}
