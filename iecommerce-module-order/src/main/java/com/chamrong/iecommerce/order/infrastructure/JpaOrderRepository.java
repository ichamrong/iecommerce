package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for OrderEntity. Prefer JpaOrderAdapter (OrderRepositoryPort) for
 * domain Order.
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {

  Optional<OrderEntity> findByCode(String code);

  List<OrderEntity> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);
}
