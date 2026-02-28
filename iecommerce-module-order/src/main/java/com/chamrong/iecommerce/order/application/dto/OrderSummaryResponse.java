package com.chamrong.iecommerce.order.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

/**
 * Lightweight read model for order list endpoints.
 *
 * <p>Deliberately excludes order items to prevent N+1 fetches on list queries. Use {@link
 * OrderResponse} (the full model) for single-order detail endpoints.
 */
public record OrderSummaryResponse(
    Long id,
    String code,
    Long customerId,
    String state,
    Money total,
    Instant confirmedAt,
    Instant shippedAt,
    Instant cancelledAt,
    Instant createdAt,
    Instant updatedAt) {}
