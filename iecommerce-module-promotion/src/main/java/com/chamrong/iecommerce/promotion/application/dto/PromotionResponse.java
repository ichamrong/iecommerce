package com.chamrong.iecommerce.promotion.application.dto;

import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import com.chamrong.iecommerce.promotion.domain.model.PromotionType;
import java.math.BigDecimal;
import java.time.Instant;

public record PromotionResponse(
    Long id,
    String name,
    String description,
    PromotionType type,
    BigDecimal value,
    String code,
    Instant validFrom,
    Instant validTo,
    PromotionStatus status,
    int priority,
    boolean stackable,
    Integer usageLimit,
    Integer usedCount) {}
