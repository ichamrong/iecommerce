package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.ports.OrderSagaStatePort;
import com.chamrong.iecommerce.order.domain.saga.OrderSagaState;
import com.chamrong.iecommerce.order.domain.saga.SagaStep;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/** JPA implementation for tracking saga choreography state. */
@Component
@RequiredArgsConstructor
public class JpaOrderSagaStateAdapter implements OrderSagaStatePort {

  private final SagaSpringDataRepository repository;

  @Override
  public void upsert(Long orderId, SagaStep step, String status) {
    OrderSagaState state =
        repository.findByOrderId(orderId).orElseGet(() -> OrderSagaState.start(orderId, step));

    state.advance(step);
    // Explicitly set status if provided (e.g. COMPENSATING, DONE, FAILED)
    if ("COMPENSATING".equals(status)) {
      state.beginCompensation(null);
    } else if ("DONE".equals(status)) {
      state.complete();
    } else if ("FAILED".equals(status)) {
      state.fail(null);
    }

    repository.save(state);
  }

  @Override
  public void recordCompensation(Long orderId, String reason) {
    repository
        .findByOrderId(orderId)
        .ifPresent(
            s -> {
              s.beginCompensation(reason);
              repository.save(s);
            });
  }

  @Override
  public Optional<OrderSagaState> findByOrderId(Long orderId) {
    return repository.findByOrderId(orderId);
  }

  @Repository
  interface SagaSpringDataRepository extends JpaRepository<OrderSagaState, Long> {
    Optional<OrderSagaState> findByOrderId(Long orderId);
  }
}
