package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.repository;

import com.chamrong.iecommerce.payment.domain.PaymentOutboxEvent;
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
 * Spring Data JPA repository for the payment outbox.
 *
 * <p>This interface belongs in infrastructure, NOT in domain. The domain only knows {@code
 * PaymentOutboxPort} (a pure interface).
 *
 * <p>Note: References {@code PaymentOutboxEvent} (legacy domain class with @Entity). Phase 2
 * migration target: move @Entity to {@code PaymentOutboxEventEntity} in infrastructure.
 */
public interface SpringDataPaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, Long> {

  @Query("SELECT e FROM PaymentOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.id ASC")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
  List<PaymentOutboxEvent> findPendingForUpdate(Pageable pageable);

  @Query("SELECT e FROM PaymentOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<PaymentOutboxEvent> findPending(PageRequest pageRequest);

  default List<PaymentOutboxEvent> findPending(int batchSize) {
    return findPending(PageRequest.of(0, batchSize));
  }
}
