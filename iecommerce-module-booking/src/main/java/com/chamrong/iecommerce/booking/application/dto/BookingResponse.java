package com.chamrong.iecommerce.booking.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record BookingResponse(
    Long id,
    String code,
    Long resourceProductId,
    Long resourceVariantId,
    Long assignedStaffId,
    Long customerId,
    Instant startAt,
    Instant endAt,
    String type,
    String status,
    Money totalPrice,
    String customerNotes,
    Instant createdAt,
    Instant updatedAt) {}
