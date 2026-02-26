package com.chamrong.iecommerce.order.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request to add a line item to a draft order. */
public record AddItemRequest(
    @NotNull(message = "Product variant ID is required") Long productVariantId,
    @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 9999, message = "Quantity cannot exceed 9999")
        Integer quantity,
    java.math.BigDecimal unitPriceAmount,
    String unitPriceCurrency,
    java.time.Instant startAt,
    java.time.Instant endAt) {}
