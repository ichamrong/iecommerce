package com.chamrong.iecommerce.order.application.dto;

/**
 * Request to ship an order.
 *
 * @param trackingNumber required; must be non-blank
 * @param idempotencyKey client-generated UUID for deduplication; maps to {@code X-Idempotency-Key}
 */
public record ShipOrderRequest(String trackingNumber, String requestId) {}
