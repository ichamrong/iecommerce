package com.chamrong.iecommerce.audit.api;

import com.chamrong.iecommerce.audit.application.dto.AuditEventRequest;
import com.chamrong.iecommerce.audit.application.dto.AuditEventResponse;
import com.chamrong.iecommerce.audit.application.dto.AuditSearchFilters;
import com.chamrong.iecommerce.audit.application.usecase.AuditUseCase;
import com.chamrong.iecommerce.audit.domain.model.AuditActor;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.common.logging.LoggingHelper;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.security.TenantGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bank-grade audit API: record events (POST), list with cursor (GET), get by id, verify tamper.
 * Tenant from TenantContext only; no tenantId in request body.
 */
@Tag(name = "Audit", description = "Enterprise audit events: record, list, verify tamper")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditApiController {

  private final AuditUseCase auditUseCase;

  @Operation(
      summary = "Record audit event",
      description = "Internal or AUDIT_WRITE. Tenant and actor from context.")
  @PostMapping("/events")
  @PreAuthorize(Permissions.HAS_AUDIT_WRITE)
  public ResponseEntity<AuditEventResponse> record(
      @Valid @RequestBody AuditEventRequest request, HttpServletRequest httpRequest) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    AuditActor actor = currentActor();
    String correlationId =
        LoggingHelper.getCorrelationId() != null ? LoggingHelper.getCorrelationId() : "";
    String ip = ipAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");
    if (userAgent != null && userAgent.length() > 500) userAgent = userAgent.substring(0, 500);

    var event = auditUseCase.record(request, tenantId, actor, correlationId, ip, userAgent);
    AuditEventResponse response = toResponse(event);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "List audit events", description = "Cursor-paginated. Requires AUDIT_READ.")
  @GetMapping("/events")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public CursorPageResponse<AuditEventResponse> list(
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(required = false) String actorId,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) String outcome,
      @RequestParam(required = false) String severity,
      @RequestParam(required = false) String targetType,
      @RequestParam(required = false) String targetId,
      @RequestParam(required = false) Instant dateFrom,
      @RequestParam(required = false) Instant dateTo,
      @RequestParam(required = false) String searchTerm) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    AuditSearchFilters filters =
        new AuditSearchFilters(
            actorId,
            eventType,
            outcome,
            severity,
            targetType,
            targetId,
            dateFrom,
            dateTo,
            searchTerm);
    return auditUseCase.list(tenantId, filters, cursor, Math.min(100, Math.max(1, limit)));
  }

  @Operation(summary = "Get audit event by ID", description = "Tenant-scoped; 404 if other tenant.")
  @GetMapping("/events/{id}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public ResponseEntity<AuditEventResponse> getById(@PathVariable Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    return auditUseCase
        .getById(tenantId, id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Verify tamper", description = "Returns event with hashValid true/false.")
  @GetMapping("/verify/{id}")
  @PreAuthorize(Permissions.HAS_AUDIT_READ)
  public ResponseEntity<AuditEventResponse> verify(@PathVariable Long id) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    return auditUseCase
        .verify(tenantId, id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  private static AuditActor currentActor() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
      return AuditActor.user(auth.getName(), null);
    }
    return AuditActor.system();
  }

  private static String ipAddress(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
    else ip = ip.split(",")[0].trim();
    return ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip;
  }

  private static AuditEventResponse toResponse(
      com.chamrong.iecommerce.audit.domain.model.AuditEvent event) {
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
        null);
  }
}
