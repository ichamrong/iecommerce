package com.chamrong.iecommerce.payment.domain.paymentintent;

import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port (Repository) for persisting and retrieving {@link PaymentIntent} aggregates.
 *
 * <p>Implementations live in {@code infrastructure/persistence/jpa/adapter/}. This interface must
 * remain free of any JPA or Spring annotations.
 */
public interface PaymentIntentRepository {

  /**
   * Persists or updates a PaymentIntent aggregate.
   *
   * @param intent the aggregate to save; must not be null
   */
  void save(PaymentIntent intent);

  /**
   * Finds a PaymentIntent by its internal unique ID.
   *
   * @param intentId the UUID of the intent
   * @return an Optional containing the intent if found
   */
  Optional<PaymentIntent> findById(UUID intentId);

  /**
   * Finds an intent by tenant + idempotency key for exactly-once initiation.
   *
   * @param tenantId the tenant identifier
   * @param idempotencyKey the client-provided idempotency key
   * @return an Optional containing the matching intent
   */
  Optional<PaymentIntent> findByIdempotencyKey(String tenantId, String idempotencyKey);

  /**
   * Finds an intent by its external provider reference ID.
   *
   * @param externalId the provider-assigned identifier
   * @return an Optional containing the matching intent
   */
  Optional<PaymentIntent> findByExternalId(String externalId);

  /**
   * Returns all intents for a tenant within a time window.
   *
   * @param tenantId the tenant identifier
   * @param start inclusive start time
   * @param end exclusive end time
   * @return a list of matching intents
   */
  List<PaymentIntent> findByTenantIdAndCreatedAtBetween(
      String tenantId, Instant start, Instant end);

  /**
   * Cursor (keyset) pagination — O(log N) index seek.
   *
   * @param tenantId the tenant identifier
   * @param lastCreatedAt the {@code created_at} of the last item on the previous page
   * @param lastId the UUID of the last item (tie-breaker)
   * @param limit maximum number of items to return
   * @return the next page of intents
   */
  List<PaymentIntent> findNextPage(String tenantId, Instant lastCreatedAt, UUID lastId, int limit);

  /**
   * Finds all intents for a given provider type within a tenant.
   *
   * @param tenantId the tenant identifier
   * @param provider the provider type
   * @return a list of matching intents
   */
  List<PaymentIntent> findByTenantIdAndProvider(String tenantId, ProviderType provider);
}
