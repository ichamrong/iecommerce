package com.chamrong.iecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateVariantRequest(
    String sku,
    BigDecimal priceAmount,
    String priceCurrency,
    BigDecimal compareAtPriceAmount,
    String compareAtPriceCurrency,
    BigDecimal weightGrams,
    Boolean enabled,
    Integer sortOrder,
    Map<String, String> translationNames) {}
