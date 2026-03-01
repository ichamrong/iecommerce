package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaIdempotencyRepository extends JpaRepository<SaleIdempotencyEntity, Long> {
  Optional<SaleIdempotencyEntity> findByTenantIdAndIdempotencyKeyAndEndpointName(
      String tenantId, String idempotencyKey, String endpointName);
}
