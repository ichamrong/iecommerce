package com.chamrong.iecommerce.payment.domain;

import com.chamrong.iecommerce.common.Money;

/**
 * Saga Event: Fired when payment is successfully captured/settled. Order module listens to this to
 * move the order to 'Confirmed/PaymentSettled' state.
 */
public record PaymentSucceededEvent(
    Long orderId, String tenantId, Long paymentId, Money amount, String externalId) {}
