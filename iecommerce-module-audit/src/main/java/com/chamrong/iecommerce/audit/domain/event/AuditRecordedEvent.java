package com.chamrong.iecommerce.audit.domain.event;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import java.time.Instant;

/**
 * Domain event published when an audit record is persisted. Optional; for outbox/event bus
 * integration.
 *
 * @param eventId persisted audit event id
 * @param tenantId tenant
 * @param eventType event type code
 * @param occurredAt when the event was recorded
 */
public record AuditRecordedEvent(
    long eventId, String tenantId, String eventType, Instant occurredAt) {

  public static AuditRecordedEvent from(AuditEvent event) {
    if (event.getId() == null) {
      throw new IllegalArgumentException("AuditEvent must have id");
    }
    return new AuditRecordedEvent(
        event.getId(), event.getTenantId(), event.getEventType(), event.getCreatedAt());
  }
}
