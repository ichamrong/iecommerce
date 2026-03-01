package com.chamrong.iecommerce.sale.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record SaleSessionResponse(
    Long id,
    Long shiftId,
    String cashierId,
    String terminalId,
    Instant startTime,
    Instant endTime,
    String status,
    Money expectedAmount,
    Money actualAmount) {}
