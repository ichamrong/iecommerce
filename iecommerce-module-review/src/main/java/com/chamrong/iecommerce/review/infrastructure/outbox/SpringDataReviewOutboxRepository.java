package com.chamrong.iecommerce.review.infrastructure.outbox;

import com.chamrong.iecommerce.review.domain.ReviewOutboxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

/**
 * Spring Data JPA repository for the review outbox table.
 *
 * <p>Belongs in infrastructure; the domain only depends on {@code ReviewOutboxPort}.
 */
public interface SpringDataReviewOutboxRepository extends JpaRepository<ReviewOutboxEvent, Long> {

  @Query("SELECT e FROM ReviewOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.id ASC")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
  List<ReviewOutboxEvent> findPendingForUpdate(Pageable pageable);

  @Query("SELECT e FROM ReviewOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<ReviewOutboxEvent> findPending(PageRequest pageRequest);

  default List<ReviewOutboxEvent> findPending(int batchSize) {
    return findPending(PageRequest.of(0, batchSize));
  }
}
