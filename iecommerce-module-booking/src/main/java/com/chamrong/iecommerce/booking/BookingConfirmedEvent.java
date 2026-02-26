package com.chamrong.iecommerce.booking;

/**
 * Published when a booking is confirmed by the provider.
 *
 * <p>Consumed by:
 *
 * <ul>
 *   <li>{@code notification} — send confirmation email/SMS to customer
 *   <li>{@code audit} — log the confirmation action
 * </ul>
 */
public record BookingConfirmedEvent(
    String tenantId,
    Long bookingId,
    Long customerId,
    Long resourceProductId,
    Long resourceVariantId,
    java.time.Instant startAt,
    java.time.Instant endAt,
    com.chamrong.iecommerce.common.Money totalPrice) {}
