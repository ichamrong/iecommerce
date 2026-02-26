package com.chamrong.iecommerce.common.event;

import com.chamrong.iecommerce.common.Money;

public record PaymentSucceededEvent(
    Long orderId, String tenantId, Long paymentId, Money amount, String externalId) {}
