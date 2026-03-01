package com.chamrong.iecommerce.sale.domain.event;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record QuotationConfirmedEvent(
    Long quotationId,
    String tenantId,
    String customerId,
    Money totalAmount,
    Instant occurrenceTime) {}
