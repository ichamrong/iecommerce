package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link InvoiceOutboxEvent}. SKIP LOCKED query ensures
 * multi-instance safety.
 */
@Repository
public interface SpringDataInvoiceOutboxRepository extends JpaRepository<InvoiceOutboxEvent, Long> {

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
