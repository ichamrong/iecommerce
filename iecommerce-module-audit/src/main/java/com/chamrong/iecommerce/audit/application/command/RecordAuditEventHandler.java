package com.chamrong.iecommerce.audit.application.command;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.model.AuditOutcome;
import com.chamrong.iecommerce.audit.domain.model.AuditSeverity;
import com.chamrong.iecommerce.audit.domain.ports.AuditEventRepositoryPort;
import com.chamrong.iecommerce.audit.domain.ports.AuditPublisherPort;
import com.chamrong.iecommerce.audit.domain.ports.AuditTamperProofPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles RecordAuditEventCommand: validates, builds domain event, resolves prevHash, computes
 * hash, saves, optionally publishes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordAuditEventHandler {

  private final AuditEventValidator validator;
  private final AuditEventRepositoryPort repository;
  private final AuditTamperProofPort tamperProof;
  private final AuditPublisherPort publisher;

  @Transactional
  public AuditEvent handle(RecordAuditEventCommand command) {
    validator.validate(command.request());

    String prevHash = repository.findPreviousHashForTenant(command.tenantId()).orElse(null);

    AuditEvent event =
        AuditEvent.builder()
            .tenantId(command.tenantId())
            .createdAt(Instant.now())
            .correlationId(command.correlationId())
            .actor(command.actor())
            .eventType(command.request().eventType())
            .outcome(AuditOutcome.valueOf(command.request().outcome()))
            .severity(AuditSeverity.valueOf(command.request().severity()))
            .target(command.targetFromRequest())
            .sourceModule(
                command.request().sourceModule() != null ? command.request().sourceModule() : "")
            .sourceEndpoint(
                command.request().sourceEndpoint() != null
                    ? command.request().sourceEndpoint()
                    : "")
            .ipAddress(truncate(command.ipAddress(), 45))
            .userAgent(truncate(command.userAgent(), 500))
            .metadataJson(command.request().metadataJson())
            .prevHash(prevHash)
            .build();

    String hash = tamperProof.computeHash(event);
    event =
        AuditEvent.builder()
            .id(null)
            .tenantId(event.getTenantId())
            .createdAt(event.getCreatedAt())
            .correlationId(event.getCorrelationId())
            .actor(event.getActor())
            .eventType(event.getEventType())
            .outcome(event.getOutcome())
            .severity(event.getSeverity())
            .target(event.getTarget())
            .sourceModule(event.getSourceModule())
            .sourceEndpoint(event.getSourceEndpoint())
            .ipAddress(event.getIpAddress())
            .userAgent(event.getUserAgent())
            .metadataJson(event.getMetadataJson())
            .prevHash(event.getPrevHash())
            .hash(hash)
            .build();

    AuditEvent saved = repository.save(event);
    try {
      publisher.publish(com.chamrong.iecommerce.audit.domain.event.AuditRecordedEvent.from(saved));
    } catch (Exception e) {
      log.warn("Audit publish failed (non-fatal): {}", e.getMessage());
    }
    return saved;
  }

  private static String truncate(String s, int maxLen) {
    if (s == null) return null;
    return s.length() <= maxLen ? s : s.substring(0, maxLen);
  }
}
