package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import com.chamrong.iecommerce.order.domain.OrderOutboxRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

interface SpringOrderOutboxRepo extends JpaRepository<OrderOutboxEvent, Long> {
  @Query(
      "SELECT e FROM OrderOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC LIMIT"
          + " :limit")
  List<OrderOutboxEvent> findPendingOrdered(int limit);
}

@Repository
class JpaOrderOutboxRepository implements OrderOutboxRepository {

  private final SpringOrderOutboxRepo jpa;

  JpaOrderOutboxRepository(SpringOrderOutboxRepo jpa) {
    this.jpa = jpa;
  }

  @Override
  public void save(OrderOutboxEvent event) {
    jpa.save(event);
  }

  @Override
  public List<OrderOutboxEvent> findPending(int limit) {
    return jpa.findPendingOrdered(limit);
  }
}
