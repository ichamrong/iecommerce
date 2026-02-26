package com.chamrong.iecommerce.common.event;

public record PaymentFailedEvent(Long orderId, String tenantId, Long paymentId, String reason) {}
