package com.chamrong.iecommerce.sale.application.dto;

import java.time.Instant;

public record ShiftResponse(
    Long id,
    String staffId,
    String terminalId,
    Instant startTime,
    Instant endTime,
    String status) {}
