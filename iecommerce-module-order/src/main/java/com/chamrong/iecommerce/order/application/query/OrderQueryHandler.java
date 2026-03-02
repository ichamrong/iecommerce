package com.chamrong.iecommerce.order.application.query;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.order.application.dto.AuditLogResponse;
import com.chamrong.iecommerce.order.application.dto.OrderSummaryResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Handles all read-side operations for Orders and Audit Logs with cursor pagination. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryHandler {

  private static final int MAX_PAGE_SIZE = 100;
  private static final String ENDPOINT_LIST_ORDERS = "order:listByCustomer";
  private static final String ENDPOINT_LIST_AUDIT = "order:auditLog";

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;

  /** Paginated order list for a specific customer. */
  @Transactional(readOnly = true)
  public CursorPageResponse<OrderSummaryResponse> listByCustomer(
      String tenantId, Long customerId, String cursor, int limit) {
    int effectiveLimit = Math.min(Math.max(1, limit), MAX_PAGE_SIZE);
    int fetchSize = effectiveLimit + 1;

    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);
    filterMap.put("customerId", customerId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_ORDERS, filterMap);

    Instant afterCreatedAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      afterCreatedAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<com.chamrong.iecommerce.order.domain.Order> orders =
        (afterCreatedAt == null || afterId == null)
            ? orderRepository.findByCustomerFirstPage(tenantId, customerId, fetchSize)
            : orderRepository.findByCustomerNextPage(
                tenantId, customerId, afterCreatedAt, afterId, fetchSize);

    return buildCursorResponse(orders, effectiveLimit, filterHash);
  }

  /** Paginated audit log history for an order. Caller must be in same tenant as order. */
  @Transactional(readOnly = true)
  public CursorPageResponse<AuditLogResponse> listAuditLog(Long orderId, String cursor, int limit) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    TenantGuard.requireSameTenant(order.getTenantId(), tenantId);

    int effectiveLimit = Math.min(Math.max(1, limit), MAX_PAGE_SIZE);
    int fetchSize = effectiveLimit + 1;

    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);
    filterMap.put("orderId", orderId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_AUDIT, filterMap);

    Instant afterOccurredAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      afterOccurredAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<com.chamrong.iecommerce.order.domain.OrderAuditLog> logs =
        (afterOccurredAt == null || afterId == null)
            ? auditPort.findByOrderFirstPage(orderId, fetchSize)
            : auditPort.findByOrderNextPage(orderId, afterOccurredAt, afterId, fetchSize);

    return buildAuditCursorResponse(logs, effectiveLimit, filterHash);
  }

  private CursorPageResponse<OrderSummaryResponse> buildCursorResponse(
      List<com.chamrong.iecommerce.order.domain.Order> orders, int limit, String filterHash) {
    boolean hasNext = orders.size() > limit;
    List<com.chamrong.iecommerce.order.domain.Order> data =
        hasNext ? orders.subList(0, limit) : orders;

    var responseData =
        data.stream()
            .map(
                o ->
                    new OrderSummaryResponse(
                        o.getId(),
                        o.getCode(),
                        o.getCustomerId(),
                        o.getState().name(),
                        o.getTotal(),
                        o.getConfirmedAt(),
                        o.getShippedAt(),
                        o.getCancelledAt(),
                        o.getCreatedAt(),
                        o.getUpdatedAt()))
            .toList();

    String nextCursor = null;
    if (hasNext && !data.isEmpty()) {
      var last = data.get(data.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    return CursorPageResponse.of(responseData, nextCursor, hasNext, limit);
  }

  private CursorPageResponse<AuditLogResponse> buildAuditCursorResponse(
      List<com.chamrong.iecommerce.order.domain.OrderAuditLog> logs, int limit, String filterHash) {

    boolean hasNext = logs.size() > limit;
    List<com.chamrong.iecommerce.order.domain.OrderAuditLog> data =
        hasNext ? logs.subList(0, limit) : logs;

    var responseData =
        data.stream()
            .map(
                l ->
                    new AuditLogResponse(
                        l.getId(),
                        l.getOrderId(),
                        l.getFromState() != null ? l.getFromState().name() : null,
                        l.getToState().name(),
                        l.getAction(),
                        l.getPerformedBy(),
                        l.getContext(),
                        l.getOccurredAt()))
            .toList();

    String nextCursor = null;
    if (hasNext && !data.isEmpty()) {
      var last = data.get(data.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getOccurredAt(), String.valueOf(last.getId()), filterHash));
    }

    return CursorPageResponse.of(responseData, nextCursor, hasNext, limit);
  }
}
