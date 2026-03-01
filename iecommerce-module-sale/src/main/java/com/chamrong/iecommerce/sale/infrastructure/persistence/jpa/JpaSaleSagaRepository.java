package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSaleSagaRepository extends JpaRepository<SaleSagaStateEntity, Long> {
  Optional<SaleSagaStateEntity> findByTenantIdAndCorrelationId(
      String tenantId, String correlationId);
}
