package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.IdempotencyKey;
import com.chamrong.iecommerce.inventory.domain.IdempotencyPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link IdempotencyPort}.
 *
 * <p>Uses the unique constraint {@code uq_idempotency(tenant_id, operation_type, reference_id)} as
 * the final defense. Application-layer pre-check ({@link #check}) avoids the constraint violation
 * path in the normal case.
 */
@Component
@RequiredArgsConstructor
public class JpaIdempotencyAdapterImpl implements IdempotencyPort {

  private final SpringDataIdempotencyRepository jpaRepo;

  @Override
  public Optional<String> check(String tenantId, String operationType, String referenceId) {
    return jpaRepo
        .findByKey(tenantId, operationType, referenceId)
        .map(IdempotencyKey::getResultSnapshot);
  }

  @Override
  public void record(
      String tenantId, String operationType, String referenceId, String resultSnapshot) {
    // Use a try-save pattern; the unique constraint guards concurrent race
    try {
      var key =
          IdempotencyKey.of(
              tenantId, operationType, referenceId, resultSnapshot, java.time.Instant.now(), null);
      jpaRepo.save(key);
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      // Another thread already recorded this key — this is the concurrent-duplicate case.
      // Silent ignore: the business operation already succeeded in the other thread.
    }
  }
}
