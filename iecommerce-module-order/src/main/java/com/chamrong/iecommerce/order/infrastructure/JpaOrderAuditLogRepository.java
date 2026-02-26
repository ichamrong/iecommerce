package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.domain.OrderAuditLog;
import com.chamrong.iecommerce.order.domain.OrderAuditLogRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

interface SpringOrderAuditLogRepo extends JpaRepository<OrderAuditLog, Long> {
  List<OrderAuditLog> findByOrderIdOrderByOccurredAtAsc(Long orderId);
}

@Repository
class JpaOrderAuditLogRepository implements OrderAuditLogRepository {

  private final SpringOrderAuditLogRepo jpa;

  JpaOrderAuditLogRepository(SpringOrderAuditLogRepo jpa) {
    this.jpa = jpa;
  }

  @Override
  public void save(OrderAuditLog log) {
    jpa.save(log);
  }

  @Override
  public List<OrderAuditLog> findByOrderId(Long orderId) {
    return jpa.findByOrderIdOrderByOccurredAtAsc(orderId);
  }
}
