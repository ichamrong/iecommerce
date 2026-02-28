package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderIdempotency;
import com.chamrong.iecommerce.order.domain.ports.OrderIdempotencyPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

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
    OrderIdempotency entry = new OrderIdempotency();
    entry.setOperationType(operationType);
    entry.setReferenceId(referenceId);
    entry.setResultSnapshot(resultSnapshot);
    repository.save(entry);
  }

  @Repository
  interface IdempotencySpringDataRepository
      extends JpaRepository<OrderIdempotency, OrderIdempotency.IdempotencyId> {
    Optional<OrderIdempotency> findByOperationTypeAndReferenceId(
        String operationType, String referenceId);
  }
}
