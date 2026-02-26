package com.chamrong.iecommerce.promotion.application.dto;

import com.chamrong.iecommerce.promotion.domain.PromotionType;
import java.time.Instant;

public record PromotionRequest(
    String name,
    String description,
    PromotionType type,
    Double value,
    String code,
    Instant validFrom,
    Instant validTo,
    Boolean active) {}
