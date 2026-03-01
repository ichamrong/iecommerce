package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.common.Money;

public interface PaymentPort {
  void initiatePayment(String tenantId, Money amount, String correlationId);

  void refund(Long orderId, Money amount, String tenantId, String reason);
}
