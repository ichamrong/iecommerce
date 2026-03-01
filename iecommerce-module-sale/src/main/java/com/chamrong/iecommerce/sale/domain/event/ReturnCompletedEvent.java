package com.chamrong.iecommerce.sale.domain.event;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record ReturnCompletedEvent(
    Long returnId,
    String tenantId,
    Long originalOrderId,
    Money totalRefundAmount,
    Instant occurrenceTime) {}
