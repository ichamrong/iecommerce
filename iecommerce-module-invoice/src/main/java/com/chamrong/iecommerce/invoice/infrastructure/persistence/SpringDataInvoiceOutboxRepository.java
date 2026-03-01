package com.chamrong.iecommerce.invoice.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link InvoiceOutboxEvent}.
 *
 * <p>SKIP LOCKED query ensures multi-instance safety: each relay instance claims a distinct batch.
 */
@Repository
public interface SpringDataInvoiceOutboxRepository extends JpaRepository<InvoiceOutboxEvent, Long> {

  /**
   * Claims up to {@code limit} PENDING events with a next_attempt_at in the past (or null), using
   * SKIP LOCKED for multi-instance safety.
   *
   * <p>Native query (PostgreSQL-specific). For other databases, swap SKIP LOCKED with an atomic
   * status update CTE.
   *
   * @param now current timestamp for filtering next_attempt_at
   * @param limit max events to claim per polling cycle
   */
  @Query(
      value =
          "SELECT * FROM invoice_outbox_event "
              + "WHERE status = 'PENDING' "
              + "AND (next_attempt_at IS NULL OR next_attempt_at <= :now) "
              + "ORDER BY created_at ASC "
              + "LIMIT :limit "
              + "FOR UPDATE SKIP LOCKED",
      nativeQuery = true)
  List<InvoiceOutboxEvent> claimPending(@Param("now") Instant now, @Param("limit") int limit);

  Optional<InvoiceOutboxEvent> findByAggregateId(Long aggregateId);
}
