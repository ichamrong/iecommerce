package com.chamrong.iecommerce.payment.domain.webhook;

/**
 * Outbound port for webhook event deduplication.
 *
 * <p>Implementations use Redis SET NX + DB unique constraints for O(1) idempotency checks. Lives in
 * {@code infrastructure/persistence/jpa/adapter/}.
 */
public interface WebhookDeduplicationPort {

  /**
   * Attempts to mark a webhook event as seen.
   *
   * @param providerEventId the provider-assigned unique event identifier
   * @return {@code true} if this event is new (not yet seen), {@code false} if duplicate
   */
  boolean markAsProcessed(String providerEventId);

  /**
   * Checks whether an event has already been processed without marking it. Useful for audit
   * queries.
   *
   * @param providerEventId the provider-assigned unique event identifier
   * @return {@code true} if event has been processed before
   */
  boolean isAlreadyProcessed(String providerEventId);
}
