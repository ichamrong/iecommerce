package com.chamrong.iecommerce.sale.infrastructure.idempotency;

import com.chamrong.iecommerce.sale.domain.ports.IdempotencyPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.JpaIdempotencyRepository;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.SaleIdempotencyEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Infrastructure adapter that stores idempotency snapshots in the sale idempotency table. */
@Component
@RequiredArgsConstructor
public class JpaIdempotencyAdapter implements IdempotencyPort {

  private final JpaIdempotencyRepository repository;

  @Override
  public Optional<String> findSnapshot(
      String tenantId, String idempotencyKey, String endpointName, String requestHash) {
    return repository
        .findByTenantIdAndIdempotencyKeyAndEndpointName(tenantId, idempotencyKey, endpointName)
        .filter(entity -> entity.getRequestHash().equals(requestHash))
        .map(SaleIdempotencyEntity::getResponseSnapshot);
  }

  @Override
  public void saveSnapshot(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestHash,
      String responseSnapshot) {
    SaleIdempotencyEntity entity =
        new SaleIdempotencyEntity(
            tenantId, idempotencyKey, endpointName, requestHash, responseSnapshot);
    repository.save(entity);
  }
}
