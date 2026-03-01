package com.chamrong.iecommerce.promotion.application.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Breakdown of an applied promotion for explainability. */
public record AppliedPromotionBreakdown(
    Long promotionId,
    String code,
    BigDecimal amount,
    String reason,
    Map<String, BigDecimal> allocations) {}
