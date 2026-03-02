package com.chamrong.iecommerce.booking.domain.ports;

import java.util.Optional;
import java.util.function.Supplier;

/** Port for idempotent execution. Used for hold, create, confirm, cancel, checkin, checkout. */
public interface IdempotencyPort {

  /**
   * Executes the supplier idempotently. If a response exists for the key+endpoint+payload, returns
   * cached response.
   *
   * @param tenantId tenant
   * @param idempotencyKey client key
   * @param endpointName endpoint identifier
   * @param requestPayload request hash or payload
   * @param supplier operation to execute
   * @return result (new or cached)
   */
  <T> T execute(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestPayload,
      Supplier<T> supplier);

  Optional<String> getCachedResponse(
      String tenantId, String idempotencyKey, String endpointName, String requestPayload);
}
