package com.chamrong.iecommerce.sale.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ShiftResponse(
    Long id,
    String staffId,
    String terminalId,
    Instant startTime,
    Instant endTime,
    BigDecimal openingBalance,
    BigDecimal closingBalance,
    String status) {}
