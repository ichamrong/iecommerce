package com.chamrong.iecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Request body for updating a product's core fields. Null = keep existing. */
public record UpdateProductRequest(
    BigDecimal basePriceAmount,
    String basePriceCurrency,
    BigDecimal compareAtPriceAmount,
    String compareAtPriceCurrency,
    Long categoryId,
    String taxCategory,
    String tags,
    Integer serviceDurationMinutes,
    Integer requiredStaffCount,
    Map<String, CreateProductRequest.TranslationRequest> translations // null = don't touch
    ) {}
