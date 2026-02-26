package com.chamrong.iecommerce.payment.infrastructure;

import com.chamrong.iecommerce.payment.domain.Payment;
import com.chamrong.iecommerce.payment.domain.PaymentRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link PaymentRepository} port. */
@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long>, PaymentRepository {
  @Override
  List<Payment> findByOrderId(Long orderId);

  @Override
  List<Payment> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);
}
