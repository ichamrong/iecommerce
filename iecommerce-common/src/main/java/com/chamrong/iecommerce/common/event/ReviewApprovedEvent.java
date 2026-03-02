package com.chamrong.iecommerce.common.event;

/** Event published when a review is approved and becomes visible to end users. */
public record ReviewApprovedEvent(
    String tenantId, Long reviewId, Long productId, Long customerId, Long bookingId, int rating) {}
