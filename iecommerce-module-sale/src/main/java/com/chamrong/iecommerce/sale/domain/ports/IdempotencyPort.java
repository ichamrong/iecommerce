package com.chamrong.iecommerce.sale.domain.ports;

import java.util.Optional;

/**
 * Port for idempotent write operations in the sale module.
 *
 * <p>Implementations handle persistence of request/response snapshots for a `(tenantId,
 * idempotencyKey, endpointName)` tuple.
 */
public interface IdempotencyPort {

  /**
   * Look up a previously stored response snapshot for the given idempotency key.
   *
   * @param tenantId tenant identifier
   * @param idempotencyKey client-supplied idempotency key
   * @param endpointName logical endpoint or use case name
   * @param requestHash hash of the request payload
   * @return optional serialized response snapshot
   */
  Optional<String> findSnapshot(
      String tenantId, String idempotencyKey, String endpointName, String requestHash);

  /**
   * Persist a response snapshot for future idempotent requests.
   *
   * @param tenantId tenant identifier
   * @param idempotencyKey client-supplied idempotency key
   * @param endpointName logical endpoint or use case name
   * @param requestHash hash of the request payload
   * @param responseSnapshot serialized response snapshot
   */
  void saveSnapshot(
      String tenantId,
      String idempotencyKey,
      String endpointName,
      String requestHash,
      String responseSnapshot);
}
