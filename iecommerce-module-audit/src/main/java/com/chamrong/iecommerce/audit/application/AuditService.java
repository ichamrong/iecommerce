package com.chamrong.iecommerce.audit.application;

import com.chamrong.iecommerce.audit.application.dto.AuditQuery;
import com.chamrong.iecommerce.audit.application.dto.AuditResponse;
import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditRepositoryPort;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditRepositoryPort auditRepository;
  private final com.chamrong.iecommerce.common.security.DigitalSignatureService signatureService;

  @Transactional
  public void log(
      String userId, String action, String resourceType, String resourceId, String metadata) {
    var event = new AuditEvent();
    event.setUserId(userId);
    event.setAction(action);
    event.setResourceType(resourceType);
    event.setResourceId(resourceId);

    // Applying bank-level audit masking for sensitive metadata
    event.setMetadata(signatureService.mask(metadata));

    try {
      var requestAttributes = RequestContextHolder.getRequestAttributes();
      if (requestAttributes instanceof ServletRequestAttributes servletAttributes) {
        var request = servletAttributes.getRequest();
        var ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
          ip = request.getRemoteAddr();
        } else {
          ip = ip.split(",")[0].trim();
        }
        event.setIpAddress(ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip);

        var userAgent = request.getHeader("User-Agent");
        event.setUserAgent(
            userAgent != null && userAgent.length() > 500
                ? userAgent.substring(0, 500)
                : userAgent);
      }
    } catch (Exception e) {
      // Ignore context errors during async event handling
    }

    auditRepository.save(event);
  }

  @Transactional
  public void logMonetaryChange(
      String userId,
      String action,
      String resourceType,
      String resourceId,
      BigDecimal valueBefore,
      BigDecimal valueAfter,
      String currency,
      String reason) {

    String metadata =
        String.format(
            "{\"valueBefore\":%s, \"valueAfter\":%s, \"currency\":\"%s\", \"reason\":\"%s\"}",
            valueBefore, valueAfter, currency, reason != null ? reason.replace("\"", "\\\"") : "");

    this.log(userId, action, resourceType, resourceId, metadata);
  }

  @Transactional(readOnly = true)
  public Optional<AuditResponse> findById(String tenantId, Long id) {
    return auditRepository
        .findById(id)
        .map(
            event -> {
              TenantGuard.requireSameTenant(event.getTenantId(), tenantId);
              return toResponse(event);
            });
  }

  /** Endpoint key for filterHash (list all with query filters). */
  public static final String ENDPOINT_LIST_ALL = "audit:listAll";

  /** Endpoint key for filterHash (list by user). */
  public static final String ENDPOINT_LIST_BY_USER = "audit:listByUser";

  /** Endpoint key for filterHash (resource history). */
  public static final String ENDPOINT_RESOURCE_HISTORY = "audit:getResourceHistory";

  /** Endpoint key for filterHash (profile my activity). */
  public static final String ENDPOINT_MY_ACTIVITY = "audit:getMyActivity";

  /**
   * Cursor-paginated list by query (admin list all / resource history). Rejects cursor when
   * filterHash mismatches (400 INVALID_CURSOR_FILTER_MISMATCH). endpointKey should be
   * ENDPOINT_LIST_ALL or ENDPOINT_RESOURCE_HISTORY so cursors are not shared across endpoints.
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<AuditResponse> findPageByQuery(
      String tenantId,
      AuditQuery query,
      String cursor,
      int limit,
      String endpointKey,
      Map<String, Object> filters) {
    int effectiveLimit = Math.min(100, Math.max(1, limit));
    int limitPlusOne = effectiveLimit + 1;
    String filterHash =
        FilterHasher.computeHash(
            endpointKey != null ? endpointKey : ENDPOINT_LIST_ALL,
            filters != null ? filters : Map.of());
    java.time.Instant cursorCreatedAt = null;
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
    List<AuditEvent> list =
        cursorCreatedAt == null || cursorId == null
            ? auditRepository.findFirstPageByQuery(tenantId, query, limitPlusOne)
            : auditRepository.findNextPageByQuery(
                tenantId, query, cursorCreatedAt, cursorId, limitPlusOne);
    boolean hasNext = list.size() > effectiveLimit;
    List<AuditEvent> pageData = hasNext ? list.subList(0, effectiveLimit) : list;
    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      AuditEvent last = pageData.get(pageData.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }
    return CursorPageResponse.of(
        pageData.stream().map(this::toResponse).toList(), nextCursor, hasNext, effectiveLimit);
  }

  /**
   * Cursor-paginated list by user (admin by user / profile my activity). Rejects cursor when
   * filterHash mismatches.
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<AuditResponse> findPageByUserId(
      String tenantId, String userId, String cursor, int limit, Map<String, Object> filters) {
    int effectiveLimit = Math.min(100, Math.max(1, limit));
    int limitPlusOne = effectiveLimit + 1;
    String filterHash =
        FilterHasher.computeHash(ENDPOINT_LIST_BY_USER, filters != null ? filters : Map.of());
    java.time.Instant cursorCreatedAt = null;
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
    List<AuditEvent> list =
        cursorCreatedAt == null || cursorId == null
            ? auditRepository.findFirstPageByUserId(tenantId, userId, limitPlusOne)
            : auditRepository.findNextPageByUserId(
                tenantId, userId, cursorCreatedAt, cursorId, limitPlusOne);
    boolean hasNext = list.size() > effectiveLimit;
    List<AuditEvent> pageData = hasNext ? list.subList(0, effectiveLimit) : list;
    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      AuditEvent last = pageData.get(pageData.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }
    return CursorPageResponse.of(
        pageData.stream().map(this::toResponse).toList(), nextCursor, hasNext, effectiveLimit);
  }

  @Transactional(readOnly = true)
  public List<String> getUniqueActions() {
    return auditRepository.findUniqueActions();
  }

  @Transactional(readOnly = true)
  public List<String> getUniqueResourceTypes() {
    return auditRepository.findUniqueResourceTypes();
  }

  private AuditResponse toResponse(AuditEvent event) {
    return new AuditResponse(
        event.getId(),
        event.getUserId(),
        event.getAction(),
        event.getResourceType(),
        event.getResourceId(),
        event.getMetadata(),
        event.getIpAddress(),
        event.getUserAgent(),
        event.getTimestamp());
  }
}
