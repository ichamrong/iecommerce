package com.chamrong.iecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Request body for creating a new product. */
public record CreateProductRequest(

    /**
     * URL-friendly slug. Auto-generated from the English name if omitted. E.g.,
     * "samsung-galaxy-s25"
     */
    String slug,

    /** PHYSICAL | DIGITAL | SERVICE */
    String productType,
    BigDecimal basePriceAmount,
    String basePriceCurrency,
    BigDecimal compareAtPriceAmount,
    String compareAtPriceCurrency,
    Long categoryId,
    String taxCategory,
    String tags,

    /**
     * At least one locale is required. "en" is required as the platform baseline. Keys are IETF BCP
     * 47 locale tags: "en", "km", "zh", etc.
     */
    Map<String, TranslationRequest> translations,
    List<CreateVariantRequest> variants) {

  public record TranslationRequest(
      String name, // required
      String description,
      String shortDescription,
      String metaTitle,
      String metaDescription) {}

  public record CreateVariantRequest(
      String sku, // required, globally unique
      BigDecimal priceAmount,
      String priceCurrency,
      BigDecimal compareAtPriceAmount,
      String compareAtPriceCurrency,
      BigDecimal weightGrams,
      int sortOrder,
      Map<String, String> translations // locale → variant name
      ) {}
}
