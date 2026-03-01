package com.chamrong.iecommerce.payment.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, Long> {

  @Query("SELECT e FROM PaymentOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.id ASC")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({
    @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
  }) // -2 is SKIP LOCKED in some providers, or use vendor specific
  List<PaymentOutboxEvent> findPendingForUpdate(Pageable pageable);

  @Query("SELECT e FROM PaymentOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<PaymentOutboxEvent> findPending(PageRequest pageRequest);

  default List<PaymentOutboxEvent> findPending(int batchSize) {
    return findPending(PageRequest.of(0, batchSize));
  }
}
