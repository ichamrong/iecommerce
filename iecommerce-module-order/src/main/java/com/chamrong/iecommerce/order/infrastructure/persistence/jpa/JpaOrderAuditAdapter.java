package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderAuditLog;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/** Keyset-paginated JPA implementation for Order Audit Port. */
@Component
@RequiredArgsConstructor
public class JpaOrderAuditAdapter implements OrderAuditPort {

  private final AuditSpringDataRepository repository;

  @Override
  public void log(
      Long orderId,
      String tenantId,
      OrderState from,
      OrderState to,
      String action,
      String performedBy,
      String context) {
    OrderAuditLog entry =
        new OrderAuditLog(orderId, tenantId, from, to, action, performedBy, context);
    repository.save(entry);
  }

  @Override
  public List<OrderAuditLog> findByOrderFirstPage(Long orderId, int limit) {
    return repository.findFirstPage(orderId, PageRequest.of(0, limit));
  }

  @Override
  public List<OrderAuditLog> findByOrderNextPage(
      Long orderId, Instant occurredAt, Long id, int limit) {
    return repository.findNextPage(orderId, occurredAt, id, PageRequest.of(0, limit));
  }

  @Repository
  interface AuditSpringDataRepository extends JpaRepository<OrderAuditLog, Long> {

    @Query(
        "SELECT a FROM OrderAuditLog a WHERE a.orderId = :orderId ORDER BY a.occurredAt DESC, a.id"
            + " DESC")
    List<OrderAuditLog> findFirstPage(Long orderId, Pageable pageable);

    @Query(
        "SELECT a FROM OrderAuditLog a WHERE a.orderId = :orderId "
            + "AND (a.occurredAt < :occurredAt OR (a.occurredAt = :occurredAt AND a.id < :id)) "
            + "ORDER BY a.occurredAt DESC, a.id DESC")
    List<OrderAuditLog> findNextPage(Long orderId, Instant occurredAt, Long id, Pageable pageable);
  }
}
