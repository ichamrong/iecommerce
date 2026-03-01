package com.chamrong.iecommerce.payment.event;

/**
 * Saga Event: Fired when a payment attempt fails. Order module listens to this to cancel the order
 * and trigger inventory release.
 */
public record PaymentFailedEvent(Long orderId, String tenantId, Long paymentId, String reason) {}
