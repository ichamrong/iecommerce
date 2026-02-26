package com.chamrong.iecommerce.common.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published when a POS sale session is completed. Triggers Invoice issuance and Payment
 * initiation in the Saga.
 */
public record SaleSessionCompletedEvent(
    Long sessionId,
    Long orderId,
    String tenantId,
    String customerId,
    BigDecimal totalAmount,
    String currency,
    Instant completedAt) {}
