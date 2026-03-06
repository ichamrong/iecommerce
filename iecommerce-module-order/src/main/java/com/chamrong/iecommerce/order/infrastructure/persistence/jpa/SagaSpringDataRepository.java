package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.saga.OrderSagaState;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaSpringDataRepository extends JpaRepository<OrderSagaState, Long> {

  Optional<OrderSagaState> findByOrderId(Long orderId);
}
