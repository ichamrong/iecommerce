package com.chamrong.iecommerce.payment.application.dto;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;

public record PaymentResponse(
    Long id,
    Long orderId,
    Money amount,
    String method,
    String status,
    String externalId,
    String checkoutData,
    Instant createdAt) {}
