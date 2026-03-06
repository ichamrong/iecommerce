package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxSpringDataRepository extends JpaRepository<OrderOutboxEvent, Long> {

  @Query("SELECT e FROM OrderOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<OrderOutboxEvent> findPendingOrdered(Pageable pageable);
}
