package com.chamrong.iecommerce.audit.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.audit.domain.model.AuditActor;
import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.model.AuditOutcome;
import com.chamrong.iecommerce.audit.domain.model.AuditSeverity;
import com.chamrong.iecommerce.audit.domain.model.AuditTarget;
import org.springframework.stereotype.Component;

/**
 * Maps between AuditEventEntity (JPA) and AuditEvent (domain). No Spring in domain; mapper in
 * infrastructure.
 */
@Component
public class AuditEventEntityMapper {

  public AuditEvent toDomain(AuditEventEntity e) {
    if (e == null) return null;
    return AuditEvent.builder()
        .id(e.getId())
        .tenantId(e.getTenantId())
        .createdAt(e.getCreatedAt())
        .correlationId(e.getCorrelationId())
        .actor(new AuditActor(e.getActorId(), e.getActorType(), e.getActorRole() != null ? e.getActorRole() : ""))
        .eventType(e.getEventType())
        .outcome(AuditOutcome.valueOf(e.getOutcome()))
        .severity(AuditSeverity.valueOf(e.getSeverity()))
        .target(new AuditTarget(e.getTargetType(), e.getTargetId() != null ? e.getTargetId() : ""))
        .sourceModule(e.getSourceModule())
        .sourceEndpoint(e.getSourceEndpoint())
        .ipAddress(e.getIpAddress())
        .userAgent(e.getUserAgent())
        .metadataJson(e.getMetadataJson())
        .prevHash(e.getPrevHash())
        .hash(e.getHash())
        .build();
  }

  public AuditEventEntity toEntity(AuditEvent d) {
    if (d == null) return null;
    AuditEventEntity e = new AuditEventEntity();
    e.setId(d.getId());
    e.setTenantId(d.getTenantId());
    e.setCreatedAt(d.getCreatedAt());
    e.setCorrelationId(d.getCorrelationId());
    e.setActorId(d.getActor().actorId());
    e.setActorType(d.getActor().actorType());
    e.setActorRole(d.getActor().role());
    e.setEventType(d.getEventType());
    e.setOutcome(d.getOutcome().name());
    e.setSeverity(d.getSeverity().name());
    e.setTargetType(d.getTarget().targetType());
    e.setTargetId(d.getTarget().targetId());
    e.setSourceModule(d.getSourceModule());
    e.setSourceEndpoint(d.getSourceEndpoint());
    e.setIpAddress(d.getIpAddress());
    e.setUserAgent(d.getUserAgent());
    e.setMetadataJson(d.getMetadataJson());
    e.setPrevHash(d.getPrevHash());
    e.setHash(d.getHash());
    return e;
  }
}
