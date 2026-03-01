package com.chamrong.iecommerce.promotion.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/** Request to reserve a promotion. */
public record RedemptionRequest(
    @NotBlank String code,
    @NotBlank String orderId,
    @NotBlank String customerId,
    @NotBlank String redemptionKey, // For idempotency
    @NotNull Map<String, Object> context // For rule evaluation
    ) {}
