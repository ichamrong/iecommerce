package com.chamrong.iecommerce.order.infrastructure.scheduler;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEvent, Long> {

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT e FROM OrderOutboxEvent e "
          + "WHERE e.status = 'PENDING' AND e.nextAttemptAt <= :now "
          + "ORDER BY e.createdAt ASC")
  @QueryHints({
    @jakarta.persistence.QueryHint(
        name = "javax.persistence.lock.timeout",
        value = "-2") // SKIP LOCKED for Postgres
  })
  List<OrderOutboxEvent> findToBeProcessed(Instant now, PageRequest pageable);
}
