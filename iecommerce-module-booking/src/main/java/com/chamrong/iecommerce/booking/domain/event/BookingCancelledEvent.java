package com.chamrong.iecommerce.booking.domain.event;

import com.chamrong.iecommerce.common.Money;

/**
 * Published when a booking is cancelled.
 *
 * @param tenantId    tenant
 * @param bookingId   booking id
 * @param reason      cancellation reason
 * @param refundAmount refund amount (if any)
 */
public record BookingCancelledEvent(
    String tenantId,
    Long bookingId,
    String reason,
    Money refundAmount) {}
