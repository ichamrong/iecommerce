package com.chamrong.iecommerce.payment.domain;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, Long> {

  @Query("SELECT e FROM PaymentOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<PaymentOutboxEvent> findPending(PageRequest pageRequest);

  default List<PaymentOutboxEvent> findPending(int batchSize) {
    return findPending(PageRequest.of(0, batchSize));
  }
}
