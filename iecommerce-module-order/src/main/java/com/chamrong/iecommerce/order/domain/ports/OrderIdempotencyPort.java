package com.chamrong.iecommerce.order.domain.ports;

import java.util.Optional;

/**
 * Port for write-operation idempotency.
 *
 * <p>Each command handler must call {@link #check} before executing business logic, and {@link
 * #record} at the end of the transaction. A unique constraint on {@code (operation_type,
 * reference_id)} in the DB provides the final race-proof guarantee.
 *
 * <p>Pattern:
 *
 * <pre>
 *   idempotency.check("CONFIRM", requestId).ifPresent(r -&gt; return cached);
 *   // ... business logic ...
 *   idempotency.record("CONFIRM", requestId, resultSnapshot);
 * </pre>
 */
public interface OrderIdempotencyPort {

  /**
   * Returns the cached result snapshot if this operation was already processed, or empty otherwise.
   *
   * @param operationType e.g. {@code "CONFIRM"}, {@code "CANCEL"}, {@code "SHIP"}
   * @param referenceId client-provided idempotency key (e.g. UUID from {@code X-Idempotency-Key})
   */
  Optional<String> check(String operationType, String referenceId);

  /**
   * Records that the operation completed successfully. Silently swallows {@code
   * DataIntegrityViolationException} for concurrent duplicates.
   *
   * @param resultSnapshot may be empty-string if no result capture is needed
   */
  void record(String operationType, String referenceId, String resultSnapshot);
}
