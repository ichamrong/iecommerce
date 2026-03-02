package com.chamrong.iecommerce.common.event;

/** Event published when a customer submits a review and it enters the moderation workflow. */
public record ReviewSubmittedEvent(
    String tenantId, Long reviewId, Long productId, Long customerId, Long bookingId, int rating) {}
