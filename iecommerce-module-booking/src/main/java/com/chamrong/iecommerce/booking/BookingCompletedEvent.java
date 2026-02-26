package com.chamrong.iecommerce.booking;

/**
 * Event published when a booking is completed. Useful for review invitations and loyalty points.
 */
public record BookingCompletedEvent(
    String tenantId, Long bookingId, Long customerId, Long resourceProductId) {}
