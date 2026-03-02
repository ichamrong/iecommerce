package com.chamrong.iecommerce.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PaymentStateMachineTest {

  @Test
  void succeedFromCreatedGoesToSucceeded() {
    assertEquals(
        PaymentStatus.SUCCEEDED, PaymentStateMachine.onAuthorizedOrCaptured(PaymentStatus.CREATED));
  }

  @Test
  void refundOnlyFromSucceeded() {
    assertEquals(PaymentStatus.REFUNDED, PaymentStateMachine.onRefund(PaymentStatus.SUCCEEDED));

    assertThrows(PaymentException.class, () -> PaymentStateMachine.onRefund(PaymentStatus.PENDING));
  }

  @Test
  void failureFromSucceededIsIllegal() {
    assertThrows(
        PaymentException.class, () -> PaymentStateMachine.onFailure(PaymentStatus.SUCCEEDED));
  }
}
