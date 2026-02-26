package com.chamrong.iecommerce.booking.application.dto;

import com.chamrong.iecommerce.booking.domain.BookingType;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateBookingRequest(
    Long resourceProductId,
    Long resourceVariantId,
    Long assignedStaffId,
    Long customerId,
    Instant startAt,
    Instant endAt,
    BookingType type,
    BigDecimal totalPriceAmount,
    String totalPriceCurrency,
    String customerNotes) {}
