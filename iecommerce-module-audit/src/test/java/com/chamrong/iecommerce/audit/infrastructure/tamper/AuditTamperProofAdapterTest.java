package com.chamrong.iecommerce.audit.infrastructure.tamper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.audit.domain.model.AuditActor;
import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.model.AuditOutcome;
import com.chamrong.iecommerce.audit.domain.model.AuditSeverity;
import com.chamrong.iecommerce.audit.domain.model.AuditTarget;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tamper proof: modified event fails verification (hash mismatch). Same event recomputes to same
 * hash.
 */
class AuditTamperProofAdapterTest {

  private AuditTamperProofAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new AuditTamperProofAdapter();
  }

  @Test
  void computeHash_isDeterministic() {
    AuditEvent event = event("tenant1", "prevHash1");
    String hash1 = adapter.computeHash(event);
    String hash2 = adapter.computeHash(event);
    assertThat(hash1).isEqualTo(hash2).isNotBlank();
  }

  @Test
  void verifyEventHash_whenUnmodified_returnsTrue() {
    AuditEvent event = event("tenant1", "prev");
    String hash = adapter.computeHash(event);
    AuditEvent withHash =
        AuditEvent.builder()
            .id(event.getId())
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
    assertThat(adapter.verifyEventHash(withHash)).isTrue();
  }

  @Test
  void verifyEventHash_whenMetadataModified_returnsFalse() {
    AuditEvent event = event("tenant1", "prev");
    String hash = adapter.computeHash(event);
    AuditEvent tampered =
        AuditEvent.builder()
            .id(event.getId())
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
            .metadataJson("tampered")
            .prevHash(event.getPrevHash())
            .hash(hash)
            .build();
    assertThat(adapter.verifyEventHash(tampered)).isFalse();
  }

  @Test
  void verifyChainLink_whenPrevHashMatches_returnsTrue() {
    AuditEvent event = eventWithPrev("tenant1", "abc123");
    assertThat(adapter.verifyChainLink(event, "abc123")).isTrue();
  }

  @Test
  void verifyChainLink_whenPrevHashMismatch_returnsFalse() {
    AuditEvent event = eventWithPrev("tenant1", "abc123");
    assertThat(adapter.verifyChainLink(event, "wrong")).isFalse();
  }

  private static AuditEvent event(String tenantId, String prevHash) {
    return AuditEvent.builder()
        .id(1L)
        .tenantId(tenantId)
        .createdAt(Instant.parse("2025-03-01T12:00:00Z"))
        .correlationId("corr1")
        .actor(AuditActor.system())
        .eventType("TEST")
        .outcome(AuditOutcome.SUCCESS)
        .severity(AuditSeverity.INFO)
        .target(new AuditTarget("ORDER", "ord-1"))
        .sourceModule("test")
        .sourceEndpoint("")
        .ipAddress(null)
        .userAgent(null)
        .metadataJson("{}")
        .prevHash(prevHash)
        .hash(null)
        .build();
  }

  private static AuditEvent eventWithPrev(String tenantId, String prevHash) {
    return AuditEvent.builder()
        .id(2L)
        .tenantId(tenantId)
        .createdAt(Instant.parse("2025-03-01T12:01:00Z"))
        .correlationId("")
        .actor(AuditActor.system())
        .eventType("TEST")
        .outcome(AuditOutcome.SUCCESS)
        .severity(AuditSeverity.INFO)
        .target(new AuditTarget("X", "y"))
        .sourceModule("")
        .sourceEndpoint("")
        .prevHash(prevHash)
        .hash("h2")
        .build();
  }
}
