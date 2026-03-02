package com.chamrong.iecommerce.audit.application.usecase;

import com.chamrong.iecommerce.audit.application.command.RecordAuditEventCommand;
import com.chamrong.iecommerce.audit.application.command.RecordAuditEventHandler;
import com.chamrong.iecommerce.audit.application.dto.AuditEventRequest;
import com.chamrong.iecommerce.audit.application.dto.AuditEventResponse;
import com.chamrong.iecommerce.audit.application.dto.AuditSearchFilters;
import com.chamrong.iecommerce.audit.application.query.AuditQueryService;
import com.chamrong.iecommerce.audit.domain.model.AuditActor;
import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Orchestration use case: record event, get by id, list with cursor, verify tamper. */
@Service
@RequiredArgsConstructor
public class AuditUseCase {

  private final RecordAuditEventHandler recordHandler;
  private final AuditQueryService queryService;

  /** Records an audit event. Tenant and actor from context. */
  public AuditEvent record(
      AuditEventRequest request,
      String tenantId,
      AuditActor actor,
      String correlationId,
      String ipAddress,
      String userAgent) {
    RecordAuditEventCommand cmd =
        new RecordAuditEventCommand(request, tenantId, actor, correlationId, ipAddress, userAgent);
    return recordHandler.handle(cmd);
  }

  public Optional<AuditEventResponse> getById(String tenantId, Long id) {
    return queryService.findById(tenantId, id);
  }

  public Optional<AuditEventResponse> verify(String tenantId, Long id) {
    return queryService.verify(tenantId, id);
  }

  public CursorPageResponse<AuditEventResponse> list(
      String tenantId, AuditSearchFilters filters, String cursor, int limit) {
    Map<String, Object> filterMap = AuditQueryService.toFilterMap(filters);
    return queryService.findPage(tenantId, filters, cursor, limit, filterMap);
  }
}
