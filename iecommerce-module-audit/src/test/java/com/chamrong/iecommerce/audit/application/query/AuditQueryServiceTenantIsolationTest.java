package com.chamrong.iecommerce.audit.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.audit.application.dto.AuditEventResponse;
import com.chamrong.iecommerce.audit.domain.model.AuditActor;
import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.model.AuditOutcome;
import com.chamrong.iecommerce.audit.domain.model.AuditSeverity;
import com.chamrong.iecommerce.audit.domain.model.AuditTarget;
import com.chamrong.iecommerce.audit.domain.ports.AuditEventRepositoryPort;
import com.chamrong.iecommerce.audit.domain.ports.AuditTamperProofPort;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tenant isolation: tenant A cannot read tenant B's audit event (IDOR). getById and verify must
 * return empty or throw when event.tenantId != current tenant.
 */
@ExtendWith(MockitoExtension.class)
class AuditQueryServiceTenantIsolationTest {

  private static final Long EVENT_ID = 1L;
  private static final String TENANT_A = "tenant-a";
  private static final String TENANT_B = "tenant-b";

  @Mock private AuditEventRepositoryPort repository;
  @Mock private AuditTamperProofPort tamperProof;

  @InjectMocks private AuditQueryService queryService;

  @Test
  void findById_whenEventBelongsToOtherTenant_throws404() {
    AuditEvent eventTenantB = event(EVENT_ID, TENANT_B);
    when(repository.findById(EVENT_ID)).thenReturn(Optional.of(eventTenantB));

    ResponseStatusException ex =
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class, () -> queryService.findById(TENANT_A, EVENT_ID));
    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  void findById_whenEventBelongsToCurrentTenant_returnsResponse() {
    AuditEvent eventTenantA = event(EVENT_ID, TENANT_A);
    when(repository.findById(EVENT_ID)).thenReturn(Optional.of(eventTenantA));

    Optional<AuditEventResponse> result = queryService.findById(TENANT_A, EVENT_ID);

    assertThat(result).isPresent();
    assertThat(result.get().tenantId()).isEqualTo(TENANT_A);
  }

  @Test
  void verify_whenEventBelongsToOtherTenant_throws404() {
    AuditEvent eventTenantB = event(EVENT_ID, TENANT_B);
    when(repository.findById(EVENT_ID)).thenReturn(Optional.of(eventTenantB));

    ResponseStatusException ex =
        org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class, () -> queryService.verify(TENANT_A, EVENT_ID));
    assertThat(ex.getStatusCode().value()).isEqualTo(404);
  }

  private static AuditEvent event(Long id, String tenantId) {
    return AuditEvent.builder()
        .id(id)
        .tenantId(tenantId)
        .createdAt(Instant.now())
        .correlationId("")
        .actor(AuditActor.system())
        .eventType("TEST")
        .outcome(AuditOutcome.SUCCESS)
        .severity(AuditSeverity.INFO)
        .target(new AuditTarget("TARGET", "id1"))
        .sourceModule("")
        .sourceEndpoint("")
        .ipAddress(null)
        .userAgent(null)
        .metadataJson(null)
        .prevHash(null)
        .hash("h1")
        .build();
  }
}
