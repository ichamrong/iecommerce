package com.chamrong.iecommerce.audit.application.query;

import com.chamrong.iecommerce.audit.application.dto.AuditEventResponse;
import com.chamrong.iecommerce.audit.domain.model.AuditEvent;

/** Maps domain AuditEvent to API AuditEventResponse. Used by query service. */
public final class AuditEventProjection {

  private AuditEventProjection() {}

  /**
   * Maps a domain event to response DTO. hashValid is null for list/get; set by verify endpoint.
   *
   * @param event domain event
   * @param hashValid null, or result of tamper verification
   * @return response DTO
   */
  public static AuditEventResponse toResponse(AuditEvent event, Boolean hashValid) {
    return new AuditEventResponse(
        event.getId(),
        event.getTenantId(),
        event.getCreatedAt(),
        event.getCorrelationId(),
        event.getActor().actorId(),
        event.getActor().actorType(),
        event.getActor().role(),
        event.getEventType(),
        event.getOutcome().name(),
        event.getSeverity().name(),
        event.getTarget().targetType(),
        event.getTarget().targetId(),
        event.getSourceModule(),
        event.getSourceEndpoint(),
        event.getIpAddress(),
        event.getUserAgent(),
        event.getMetadataJson(),
        hashValid);
  }

  public static AuditEventResponse toResponse(AuditEvent event) {
    return toResponse(event, null);
  }
}
