package com.chamrong.iecommerce.inventory.domain;

import java.util.Optional;

/**
 * Port for deduplication of inventory write operations.
 *
 * <p>Before executing any mutating use case, the handler calls {@link #check} to see if the
 * operation has already been executed. If so, the cached result snapshot is returned. If not,
 * {@link #record} is called after successful execution.
 *
 * <p>Implementors must enforce the unique constraint {@code (tenant_id, operation_type,
 * reference_id)} at the DB level to handle concurrent retries safely.
 */
public interface IdempotencyPort {

  /**
   * Returns the cached result snapshot for a previously recorded operation, or empty if this is the
   * first attempt.
   *
   * @param tenantId required tenant scope
   * @param operationType e.g. {@code "RECEIVE"}, {@code "RESERVE"}, {@code "COMMIT"}
   * @param referenceId caller-supplied external reference (orderId, adjustmentId, etc.)
   */
  Optional<String> check(String tenantId, String operationType, String referenceId);

  /**
   * Records the result of a successfully executed operation. Must be called within the same
   * transaction as the operation itself to be atomic.
   *
   * @param tenantId required tenant scope
   * @param operationType the operation type
   * @param referenceId external reference id
   * @param resultSnapshot JSON-serialized result (may be empty string for void operations)
   */
  void record(String tenantId, String operationType, String referenceId, String resultSnapshot);
}
