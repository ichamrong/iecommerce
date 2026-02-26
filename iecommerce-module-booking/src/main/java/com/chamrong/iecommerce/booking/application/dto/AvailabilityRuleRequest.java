package com.chamrong.iecommerce.booking.application.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityRuleRequest(
    Long resourceProductId,
    Long resourceVariantId,
    Long staffId,
    DayOfWeek dayOfWeek,
    LocalTime openTime,
    LocalTime closeTime,
    Integer slotDurationMinutes) {}
