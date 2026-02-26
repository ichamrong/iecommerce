package com.chamrong.iecommerce.payment.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
  Payment save(Payment payment);

  Optional<Payment> findById(Long id);

  List<Payment> findByOrderId(Long orderId);

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);

  List<Payment> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);
}
