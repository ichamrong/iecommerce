package com.chamrong.iecommerce.promotion.application.dto;

import com.chamrong.iecommerce.promotion.domain.model.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record PromotionRequest(
    @NotBlank String name,
    String description,
    @NotNull PromotionType type,
    @NotNull BigDecimal value,
    String code,
    Instant validFrom,
    Instant validTo,
    int priority,
    boolean isStackable,
    Integer usageLimit) {}
