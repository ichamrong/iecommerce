package com.chamrong.iecommerce.order;

/**
 * Event published when an order is completed/paid. Useful for other modules like Customer (Loyalty)
 * and Audit.
 */
public record OrderCompletedEvent(
    String tenantId, Long orderId, Long customerId, int pointsEarned) {}
