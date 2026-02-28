package com.chamrong.iecommerce.order.application.dto;

import java.time.Instant;

/**
 * Read model for a single audit log entry — safe to expose via REST API.
 *
 * @param id audit log entry id
 * @param orderId the order this entry belongs to
 * @param fromState state before the transition (null for creation)
 * @param toState state after the transition
 * @param action action label (e.g. {@code ORDER_CONFIRMED})
 * @param performedBy human-readable actor (JWT subject or "system")
 * @param context optional freeform context (e.g. "items=3 total=150.00")
 * @param occurredAt timestamp of the event
 */
public record AuditLogResponse(
    Long id,
    Long orderId,
    String fromState,
    String toState,
    String action,
    String performedBy,
    String context,
    Instant occurredAt) {}
