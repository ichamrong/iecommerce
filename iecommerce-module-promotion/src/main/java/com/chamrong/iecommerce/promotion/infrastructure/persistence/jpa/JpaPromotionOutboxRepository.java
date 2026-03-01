package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.PromotionOutboxEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPromotionOutboxRepository extends JpaRepository<PromotionOutboxEvent, Long> {

  @Query(
      "SELECT e FROM PromotionOutboxEvent e WHERE e.status = 'PENDING' "
          + "AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now) "
          + "ORDER BY e.createdAt ASC")
  List<PromotionOutboxEvent> findPending(Instant now, Pageable pageable);
}
