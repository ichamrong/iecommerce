package com.chamrong.iecommerce.payment.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
  Payment save(Payment payment);

  Optional<Payment> findById(Long id);

  List<Payment> findByOrderId(Long orderId);
}
