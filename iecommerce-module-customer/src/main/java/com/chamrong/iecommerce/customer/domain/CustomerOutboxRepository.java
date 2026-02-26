package com.chamrong.iecommerce.customer.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerOutboxRepository extends JpaRepository<CustomerOutboxEvent, Long> {

  @Query("SELECT e FROM CustomerOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<CustomerOutboxEvent> findPending(Pageable pageable);

  default List<CustomerOutboxEvent> findPending(int limit) {
    return findPending(Pageable.ofSize(limit));
  }
}
