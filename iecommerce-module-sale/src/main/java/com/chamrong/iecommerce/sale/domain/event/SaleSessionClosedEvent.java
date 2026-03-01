package com.chamrong.iecommerce.sale.domain.event;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record SaleSessionClosedEvent(
    Long sessionId,
    String tenantId,
    String cashierId,
    String terminalId,
    Money expectedAmount,
    Money actualAmount,
    Instant occurrenceTime) {}
