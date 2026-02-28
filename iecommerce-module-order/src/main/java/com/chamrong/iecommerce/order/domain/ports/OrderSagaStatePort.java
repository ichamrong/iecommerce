package com.chamrong.iecommerce.order.domain.ports;

import com.chamrong.iecommerce.order.domain.saga.OrderSagaState;
import com.chamrong.iecommerce.order.domain.saga.SagaStep;
import java.util.Optional;

/**
 * Port for tracking per-order saga progress.
 *
 * <p>Used by handlers and saga listeners to record which saga step an order is in, enabling
 * observability, debugging, and safe re-processing after failures.
 */
public interface OrderSagaStatePort {

  /**
   * Upserts the saga state for an order. If no state exists, creates one. If one exists, updates
   * step/status.
   */
  void upsert(Long orderId, SagaStep step, String status);

  /**
   * Records a compensation step (e.g., inventory released, order cancelled due to saga failure).
   */
  void recordCompensation(Long orderId, String reason);

  Optional<OrderSagaState> findByOrderId(Long orderId);
}
