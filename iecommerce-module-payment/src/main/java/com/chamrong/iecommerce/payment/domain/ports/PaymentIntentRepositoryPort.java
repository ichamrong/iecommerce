package com.chamrong.iecommerce.payment.domain.ports;

import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for persisting and retrieving PaymentIntents. Implementations must ensure
 * transactional integrity and safe concurrent access.
 */
public interface PaymentIntentRepositoryPort {
  /**
   * Persists a PaymentIntent aggregate.
   *
   * @param intent the aggregate to save
   */
  void save(PaymentIntent intent);

  /**
   * Finds an intent by its internal unique ID.
   *
   * @param intentId the UUID
   * @return Optional of PaymentIntent
   */
  Optional<PaymentIntent> findById(UUID intentId);

  /**
   * Finds an intent by its idempotency key and tenant. Ensures exactly-once initiation.
   *
   * @param tenantId the tenant ID
   * @param idempotencyKey the client-provided key
   * @return Optional of PaymentIntent
   */
  Optional<PaymentIntent> findByIdempotencyKey(String tenantId, String idempotencyKey);

  Optional<PaymentIntent> findByExternalId(String externalId);

  List<PaymentIntent> findByTenantIdAndCreatedAtBetween(
      String tenantId, Instant start, Instant end);

  /**
   * Performs keyset (cursor-based) pagination for payment history.
   *
   * @param tenantId the tenant ID
   * @param lastCreatedAt the timestamp of the last item in the previous page (exclusive)
   * @param lastId the UUID of the last item (for tie-breaking)
   * @param limit maximum number of items to return
   * @return a page of PaymentIntents
   */
  List<PaymentIntent> findNextPage(String tenantId, Instant lastCreatedAt, UUID lastId, int limit);
}
