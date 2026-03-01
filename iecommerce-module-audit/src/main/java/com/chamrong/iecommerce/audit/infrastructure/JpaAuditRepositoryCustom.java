package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.domain.AuditEvent;
import java.time.Instant;
import java.util.List;

/** Custom keyset pagination methods for audit. Implemented by {@link JpaAuditRepositoryImpl}. */
public interface JpaAuditRepositoryCustom {

  List<AuditEvent> findFirstPage(String tenantId, int limitPlusOne);

  List<AuditEvent> findNextPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  List<AuditEvent> findFirstPageByUserId(String tenantId, String userId, int limitPlusOne);

  List<AuditEvent> findNextPageByUserId(
      String tenantId, String userId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  List<AuditEvent> findFirstPageByQuery(String tenantId, AuditQuery query, int limitPlusOne);

  List<AuditEvent> findNextPageByQuery(
      String tenantId, AuditQuery query, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);
}
