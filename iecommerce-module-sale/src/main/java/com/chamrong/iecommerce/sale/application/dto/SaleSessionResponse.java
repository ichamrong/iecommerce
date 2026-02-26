package com.chamrong.iecommerce.sale.application.dto;

import java.time.Instant;

public record SaleSessionResponse(
    Long id,
    Long shiftId,
    String customerId,
    Long orderId,
    Instant startTime,
    Instant endTime,
    String status,
    String reference) {}
