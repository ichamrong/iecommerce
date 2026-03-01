package com.chamrong.iecommerce.booking.domain.event;

import java.time.Instant;

/**
 * Published when a booking is created (pending confirmation).
 *
 * @param tenantId           tenant
 * @param bookingId          booking id
 * @param customerId         customer
 * @param resourceProductId  resource
 * @param startAt            start
 * @param endAt              end
 * @param totalAmount        total price amount
 * @param currency           currency
 */
public record BookingCreatedEvent(
    String tenantId,
    Long bookingId,
    Long customerId,
    Long resourceProductId,
    Instant startAt,
    Instant endAt,
    java.math.BigDecimal totalAmount,
    String currency) {}
