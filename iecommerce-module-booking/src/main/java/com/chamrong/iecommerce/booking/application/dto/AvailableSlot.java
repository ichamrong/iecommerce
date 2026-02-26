package com.chamrong.iecommerce.booking.application.dto;

import java.time.Instant;

/** A single bookable time slot returned by the availability query. */
public record AvailableSlot(Instant startAt, Instant endAt, boolean available) {}
