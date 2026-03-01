package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.payment.domain.PaymentOutboxEvent;
import com.chamrong.iecommerce.payment.domain.ports.PaymentOutboxPort;
import com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.repository.SpringDataPaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter: implements {@link PaymentOutboxPort} by persisting events to the payment outbox table.
 */
@Component
@RequiredArgsConstructor
public class JpaPaymentOutboxAdapter implements PaymentOutboxPort {

  private final SpringDataPaymentOutboxRepository outboxRepository;

  @Override
  public void save(String tenantId, String eventType, String payload) {
    outboxRepository.save(PaymentOutboxEvent.pending(tenantId, eventType, payload));
  }
}
