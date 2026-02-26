package com.chamrong.iecommerce.payment.application.spi;

public interface PaymentProvider {
  String initiatePayment(String orderId, java.math.BigDecimal amount, String currency);

  boolean supports(String method);
}
