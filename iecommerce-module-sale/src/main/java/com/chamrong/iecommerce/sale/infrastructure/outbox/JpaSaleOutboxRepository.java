package com.chamrong.iecommerce.sale.infrastructure.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSaleOutboxRepository extends JpaRepository<SaleOutboxEvent, Long> {

  @Query(
      value =
          "SELECT * FROM sales_outbox e WHERE e.status = 'PENDING' AND (e.next_attempt_at IS NULL"
              + " OR e.next_attempt_at <= CURRENT_TIMESTAMP) ORDER BY e.created_at ASC LIMIT :limit"
              + " FOR UPDATE SKIP LOCKED",
      nativeQuery = true)
  List<SaleOutboxEvent> findPendingForRelay(int limit);
}
