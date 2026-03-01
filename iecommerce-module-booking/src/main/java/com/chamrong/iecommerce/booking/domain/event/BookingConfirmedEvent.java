package com.chamrong.iecommerce.booking.domain.event;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

/**
 * Published when a booking is confirmed.
 */
public record BookingConfirmedEvent(
    String tenantId,
    Long bookingId,
    Long customerId,
    Long resourceProductId,
    Long resourceVariantId,
    Instant startAt,
    Instant endAt,
    Money totalPrice) {}
