package com.chamrong.iecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record AddVariantRequest(
    String sku,
    BigDecimal priceAmount,
    String priceCurrency,
    BigDecimal compareAtPriceAmount,
    String compareAtPriceCurrency,
    BigDecimal weightGrams,
    boolean enabled,
    int sortOrder,
    Map<String, String> translationNames) {}
