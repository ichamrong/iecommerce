package com.chamrong.iecommerce.audit.application.query;

import com.chamrong.iecommerce.audit.application.dto.AuditEventResponse;
import com.chamrong.iecommerce.audit.application.dto.AuditSearchFilters;
import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditEventRepositoryPort;
import com.chamrong.iecommerce.audit.domain.ports.AuditSearchCriteria;
import com.chamrong.iecommerce.audit.domain.ports.AuditTamperProofPort;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query service for audit events: get by id (with IDOR check), cursor-paginated list with
 * filterHash validation, and tamper verify.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditQueryService {

  /** Endpoint key for filterHash (list events). */
  public static final String ENDPOINT_LIST_EVENTS = "audit:listEvents";

  private final AuditEventRepositoryPort repository;
  private final AuditTamperProofPort tamperProof;

  @Transactional(readOnly = true)
  public Optional<AuditEventResponse> findById(String tenantId, Long id) {
    Optional<AuditEvent> opt = repository.findById(id);
    if (opt.isEmpty()) return Optional.empty();
    AuditEvent event = opt.get();
    TenantGuard.requireSameTenant(event.getTenantId(), tenantId);
    return Optional.of(AuditEventProjection.toResponse(event));
  }

  /**
   * Returns verification result for an event: VALID if hash and chain link are correct.
   *
   * @param tenantId current tenant
   * @param id       event id
   * @return response with hashValid true/false and reason if invalid
   */
  @Transactional(readOnly = true)
  public Optional<AuditEventResponse> verify(String tenantId, Long id) {
    Optional<AuditEvent> opt = repository.findById(id);
    if (opt.isEmpty()) return Optional.empty();
    AuditEvent event = opt.get();
    TenantGuard.requireSameTenant(event.getTenantId(), tenantId);

    boolean hashValid = tamperProof.verifyEventHash(event);
    if (hashValid && event.getPrevHash() != null && !event.getPrevHash().isEmpty()) {
      Optional<AuditEvent> prev =
          repository.findPreviousEventInChain(
              event.getTenantId(), event.getCreatedAt(), event.getId());
      hashValid = prev.map(p -> event.getPrevHash().equals(p.getHash())).orElse(false);
    }
    return Optional.of(AuditEventProjection.toResponse(event, hashValid));
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<AuditEventResponse> findPage(
      String tenantId,
      AuditSearchFilters filters,
      String cursor,
      int limit,
      Map<String, Object> filterMapForHash) {
    int effectiveLimit = Math.min(100, Math.max(1, limit));
    int limitPlusOne = effectiveLimit + 1;
    String filterHash =
        FilterHasher.computeHash(
            ENDPOINT_LIST_EVENTS, filterMapForHash != null ? filterMapForHash : Map.of());

    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    AuditSearchCriteria criteria = toCriteria(filters);
    List<AuditEvent> list =
        cursorCreatedAt == null || cursorId == null
            ? repository.findFirstPage(tenantId, criteria, limitPlusOne)
            : repository.findNextPage(tenantId, criteria, cursorCreatedAt, cursorId, limitPlusOne);

    boolean hasNext = list.size() > effectiveLimit;
    List<AuditEvent> pageData = hasNext ? list.subList(0, effectiveLimit) : list;
    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      AuditEvent last = pageData.get(pageData.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }
    List<AuditEventResponse> data = pageData.stream().map(AuditEventProjection::toResponse).toList();
    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

  /** Builds filter map for FilterHasher from AuditSearchFilters. */
  public static Map<String, Object> toFilterMap(AuditSearchFilters f) {
    Map<String, Object> m = new LinkedHashMap<>();
    if (f != null) {
      if (f.actorId() != null) m.put("actorId", f.actorId());
      if (f.eventType() != null) m.put("eventType", f.eventType());
      if (f.outcome() != null) m.put("outcome", f.outcome());
      if (f.severity() != null) m.put("severity", f.severity());
      if (f.targetType() != null) m.put("targetType", f.targetType());
      if (f.targetId() != null) m.put("targetId", f.targetId());
      if (f.dateFrom() != null) m.put("dateFrom", f.dateFrom());
      if (f.dateTo() != null) m.put("dateTo", f.dateTo());
      if (f.searchTerm() != null) m.put("searchTerm", f.searchTerm());
    }
    return m;
  }

  private static AuditSearchCriteria toCriteria(AuditSearchFilters f) {
    if (f == null) return AuditSearchCriteria.empty();
    return new AuditSearchCriteria(
        f.actorId(),
        f.eventType(),
        f.outcome(),
        f.severity(),
        f.targetType(),
        f.targetId(),
        f.dateFrom(),
        f.dateTo(),
        f.searchTerm());
  }
}
