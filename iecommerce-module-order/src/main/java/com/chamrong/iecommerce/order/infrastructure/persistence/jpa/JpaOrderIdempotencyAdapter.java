package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderIdempotency;
import com.chamrong.iecommerce.order.domain.ports.OrderIdempotencyPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** JPA implementation for command idempotency. */
@Component
@RequiredArgsConstructor
public class JpaOrderIdempotencyAdapter implements OrderIdempotencyPort {

  private final IdempotencySpringDataRepository repository;

  @Override
  public Optional<String> check(String operationType, String referenceId) {
    return repository
        .findByOperationTypeAndReferenceId(operationType, referenceId)
        .map(OrderIdempotency::getResultSnapshot);
  }

  @Override
  public void record(String operationType, String referenceId, String resultSnapshot) {
    repository.save(OrderIdempotency.of(operationType, referenceId, resultSnapshot));
  }
}
