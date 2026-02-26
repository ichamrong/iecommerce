package com.chamrong.iecommerce.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentStateTest {

  private Payment payment;

  @BeforeEach
  void setUp() {
    payment = new Payment();
  }

  @Test
  void testInitialState() {
    assertEquals(PaymentStatus.PENDING, payment.getStatus());
  }

  @Test
  void testMarkSucceeded() {
    payment.markSucceeded("txn_123");
    assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
    assertEquals("txn_123", payment.getExternalId());
  }

  @Test
  void testMarkFailed() {
    payment.markFailed();
    assertEquals(PaymentStatus.FAILED, payment.getStatus());
  }

  @Test
  void testRefundSucceededPayment() {
    payment.markSucceeded("txn_123");
    payment.markRefunded();
    assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
  }

  @Test
  void testRefundNonSucceededPaymentThrowsException() {
    // Attempting to refund a PENDING payment
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> payment.markRefunded());
    assertEquals("Only succeeded payments can be refunded", exception.getMessage());

    // Attempting to refund a FAILED payment
    payment.markFailed();
    exception = assertThrows(IllegalStateException.class, () -> payment.markRefunded());
    assertEquals("Only succeeded payments can be refunded", exception.getMessage());
  }
}
