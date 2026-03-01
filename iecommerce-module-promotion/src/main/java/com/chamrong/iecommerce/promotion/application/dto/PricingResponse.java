package com.chamrong.iecommerce.promotion.application.dto;

import java.math.BigDecimal;
import java.util.List;

/** Detailed response for pricing requests. */
public record PricingResponse(
    BigDecimal totalBeforeDiscount,
    BigDecimal totalDiscount,
    BigDecimal totalAfterDiscount,
    List<AppliedPromotionBreakdown> appliedPromotions) {}
