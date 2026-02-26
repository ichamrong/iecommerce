package com.chamrong.iecommerce.inventory.domain;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryOutboxRepository extends JpaRepository<InventoryOutboxEvent, Long> {

  @Query("SELECT e FROM InventoryOutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
  List<InventoryOutboxEvent> findPending(PageRequest pageRequest);
  
  default List<InventoryOutboxEvent> findPending(int batchSize) {
    return findPending(PageRequest.of(0, batchSize));
  }
}
