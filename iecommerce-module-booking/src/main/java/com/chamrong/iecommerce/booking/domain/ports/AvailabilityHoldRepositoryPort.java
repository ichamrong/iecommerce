package com.chamrong.iecommerce.booking.domain.ports;

import com.chamrong.iecommerce.booking.domain.model.AvailabilityHold;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for availability hold persistence. Holds expire and must be released by job or on booking
 * create.
 */
public interface AvailabilityHoldRepositoryPort {

  AvailabilityHold save(AvailabilityHold hold);

  Optional<AvailabilityHold> findByHoldToken(String tenantId, String holdToken);

  Optional<AvailabilityHold> findByIdempotencyKey(String tenantId, String idempotencyKey);

  List<AvailabilityHold> findExpiredBefore(Instant before);

  void delete(AvailabilityHold hold);

  /** Check if resource is held (non-expired) for the given window. */
  boolean existsActiveHold(
      String tenantId,
      Long resourceProductId,
      Long resourceVariantId,
      Long resourceId,
      Instant start,
      Instant end,
      String excludeHoldToken);
}
