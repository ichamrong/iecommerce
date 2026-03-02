package com.chamrong.iecommerce.common.event;

/** Event published when a review is rejected during moderation. */
public record ReviewRejectedEvent(
    String tenantId, Long reviewId, Long productId, Long customerId, Long bookingId, int rating) {}
