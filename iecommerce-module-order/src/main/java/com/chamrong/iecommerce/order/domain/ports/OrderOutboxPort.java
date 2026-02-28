package com.chamrong.iecommerce.order.domain.ports;

/**
 * Port for writing domain events to the outbox table.
 *
 * <p>Must be called in the same transaction as the Order save. The infrastructure adapter
 * serializes the payload to JSON and persists it with status=PENDING. The hardened relay scheduler
 * picks it up using {@code SELECT … FOR UPDATE SKIP LOCKED}.
 */
public interface OrderOutboxPort {

  /**
   * Writes an event to the outbox.
   *
   * @param tenantId tenant scope
   * @param aggregateId the order ID (stored as {@code aggregate_id} column)
   * @param eventType canonical event class name (e.g., {@code "OrderConfirmedEvent"})
   * @param payload serialized JSON payload
   */
  void publish(String tenantId, Long aggregateId, String eventType, String payload);
}
