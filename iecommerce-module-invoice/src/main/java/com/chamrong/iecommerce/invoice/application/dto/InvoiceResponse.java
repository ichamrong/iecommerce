package com.chamrong.iecommerce.invoice.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record InvoiceResponse(
    Long id,
    String invoiceNumber,
    Long orderId,
    Instant invoiceDate,
    String status,
    Money totalAmount,
    Instant createdAt) {}
