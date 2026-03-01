package com.chamrong.iecommerce.audit.domain.ports;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.domain.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Audit persistence port. List methods use keyset pagination (created_at DESC, id DESC).
 *
 * <p>Implementations MUST enforce tenant isolation for all queries.
 */
public interface AuditRepositoryPort {

  AuditEvent save(AuditEvent event);

  Optional<AuditEvent> findById(Long id);

  /** First page — no cursor. Returns at most limitPlusOne rows. */
  List<AuditEvent> findFirstPage(String tenantId, int limitPlusOne);

  /** Next page — keyset cursor. Returns at most limitPlusOne rows. */
  List<AuditEvent> findNextPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  List<AuditEvent> findFirstPageByUserId(String tenantId, String userId, int limitPlusOne);

  List<AuditEvent> findNextPageByUserId(
      String tenantId, String userId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  List<AuditEvent> findFirstPageByQuery(String tenantId, AuditQuery query, int limitPlusOne);

  List<AuditEvent> findNextPageByQuery(
      String tenantId, AuditQuery query, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  List<String> findUniqueActions();

  List<String> findUniqueResourceTypes();
}
