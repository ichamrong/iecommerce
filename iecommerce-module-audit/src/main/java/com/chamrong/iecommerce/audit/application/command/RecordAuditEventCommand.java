package com.chamrong.iecommerce.audit.application.command;

import com.chamrong.iecommerce.audit.application.dto.AuditEventRequest;
import com.chamrong.iecommerce.audit.domain.model.AuditActor;
import com.chamrong.iecommerce.audit.domain.model.AuditTarget;
import java.util.Objects;

/**
 * Command to record a single audit event. Tenant and actor come from context (not from request).
 *
 * @param request validated request body
 * @param tenantId from TenantContext
 * @param actor from security context
 * @param correlationId from MDC
 * @param ipAddress optional
 * @param userAgent optional
 */
public record RecordAuditEventCommand(
    AuditEventRequest request,
    String tenantId,
    AuditActor actor,
    String correlationId,
    String ipAddress,
    String userAgent) {

  public RecordAuditEventCommand {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(tenantId, "tenantId");
    Objects.requireNonNull(actor, "actor");
    correlationId = correlationId != null ? correlationId : "";
  }

  public AuditTarget targetFromRequest() {
    return new AuditTarget(
        request.targetType(), request.targetId() != null ? request.targetId() : "");
  }
}
