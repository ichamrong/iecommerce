package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderIdempotency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencySpringDataRepository
    extends JpaRepository<OrderIdempotency, OrderIdempotency.IdempotencyId> {

  @Query(
      "SELECT e FROM OrderIdempotency e WHERE e.id.operationType = :operationType AND"
          + " e.id.referenceId = :referenceId")
  Optional<OrderIdempotency> findByOperationTypeAndReferenceId(
      String operationType, String referenceId);
}
