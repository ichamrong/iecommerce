package com.chamrong.iecommerce.audit.domain.ports;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for persisting and querying audit events. All queries MUST be tenant-scoped.
 *
 * <p>Keyset pagination: created_at DESC, id DESC. Implementations enforce tenant isolation.
 */
public interface AuditEventRepositoryPort {

  /**
   * Saves an audit event (append-only). Implementations set tenantId from context if not set.
   *
   * @param event event to persist (id null for insert)
   * @return persisted event with id and createdAt
   */
  AuditEvent save(AuditEvent event);

  /**
   * Finds an event by id. Caller must enforce tenant check (IDOR: event.tenantId ==
   * TenantContext).
   *
   * @param id event id
   * @return event if found
   */
  Optional<AuditEvent> findById(Long id);

  /**
   * First page for tenant; no cursor. Returns at most limitPlusOne rows.
   *
   * @param tenantId   current tenant (required)
   * @param filters    optional filters (actorId, eventType, outcome, severity, targetType, targetId,
   *                   dateFrom, dateTo)
   * @param limitPlusOne limit + 1 to detect hasNext
   * @return list ordered by created_at DESC, id DESC
   */
  List<AuditEvent> findFirstPage(String tenantId, AuditSearchCriteria filters, int limitPlusOne);

  /**
   * Next page using keyset cursor.
   *
   * @param tenantId       current tenant
   * @param filters        same as first page
   * @param cursorCreatedAt last item's created_at
   * @param cursorId        last item's id
   * @param limitPlusOne   limit + 1
   * @return list ordered by created_at DESC, id DESC
   */
  List<AuditEvent> findNextPage(
      String tenantId,
      AuditSearchCriteria filters,
      Instant cursorCreatedAt,
      Long cursorId,
      int limitPlusOne);

  /**
   * Returns the hash of the most recent event for the tenant (for hash chain when inserting next
   * event). Empty if no events yet.
   */
  Optional<String> findPreviousHashForTenant(String tenantId);

  /**
   * Returns the event immediately before the given (createdAt, id) in ascending order (so its hash
   * should equal the given event's prevHash). Used for chain verification.
   */
  Optional<AuditEvent> findPreviousEventInChain(String tenantId, Instant createdAt, Long id);
}
