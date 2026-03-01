package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.domain.Order;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for Order. Used by JpaOrderAdapter (OrderRepositoryPort). */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByCode(String code);

  List<Order> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);
}
