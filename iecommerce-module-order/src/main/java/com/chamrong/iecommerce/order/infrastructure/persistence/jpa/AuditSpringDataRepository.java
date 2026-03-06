package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.OrderAuditLog;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditSpringDataRepository extends JpaRepository<OrderAuditLog, Long> {

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
