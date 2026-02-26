package com.chamrong.iecommerce.booking;

import java.time.Instant;

/** Event published when a booking is approaching its start time. */
public record BookingReminderEvent(
    String tenantId, Long bookingId, Long customerId, Instant startAt) {}
