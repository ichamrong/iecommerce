package com.chamrong.iecommerce.order.application.query;

import com.chamrong.iecommerce.order.application.dto.AuditLogResponse;
import com.chamrong.iecommerce.order.application.dto.OrderCursorResponse;
import com.chamrong.iecommerce.order.application.dto.OrderSummaryResponse;
import com.chamrong.iecommerce.order.application.util.OrderCursorEncoder;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles all read-side operations for Orders and Audit Logs. Implements keyset pagination logic
 * using {@link OrderCursorEncoder}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryHandler {

  private final OrderRepositoryPort orderRepository;
  private final OrderAuditPort auditPort;

  /** Paginated order list for a specific customer. */
  @Transactional(readOnly = true)
  public OrderCursorResponse<OrderSummaryResponse> listByCustomer(
      String tenantId, Long customerId, String cursor, int limit) {
    int fetchSize = limit + 1;
    var decoded = OrderCursorEncoder.decode(cursor);

    List<com.chamrong.iecommerce.order.domain.Order> orders =
        (decoded == null)
            ? orderRepository.findByCustomerFirstPage(tenantId, customerId, fetchSize)
            : orderRepository.findByCustomerNextPage(
                tenantId, customerId, decoded.ts(), decoded.id(), fetchSize);

    return buildCursorResponse(orders, limit);
  }

  /** Paginated audit log history for an order. */
  @Transactional(readOnly = true)
  public OrderCursorResponse<AuditLogResponse> listAuditLog(
      Long orderId, String cursor, int limit) {
    int fetchSize = limit + 1;
    var decoded = OrderCursorEncoder.decode(cursor);

    List<com.chamrong.iecommerce.order.domain.OrderAuditLog> logs =
        (decoded == null)
            ? auditPort.findByOrderFirstPage(orderId, fetchSize)
            : auditPort.findByOrderNextPage(orderId, decoded.ts(), decoded.id(), fetchSize);

    return buildAuditCursorResponse(logs, limit, orderId);
  }

  private OrderCursorResponse<OrderSummaryResponse> buildCursorResponse(
      List<com.chamrong.iecommerce.order.domain.Order> orders, int limit) {
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
    if (hasNext) {
      var last = data.get(data.size() - 1);
      nextCursor = OrderCursorEncoder.encode(last.getCreatedAt(), last.getId());
    }

    return new OrderCursorResponse<>(responseData, nextCursor, hasNext);
  }

  private OrderCursorResponse<AuditLogResponse> buildAuditCursorResponse(
      List<com.chamrong.iecommerce.order.domain.OrderAuditLog> logs, int limit, Long orderId) {

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
    if (hasNext) {
      var last = data.get(data.size() - 1);
      nextCursor = OrderCursorEncoder.encode(last.getOccurredAt(), last.getId());
    }

    return new OrderCursorResponse<>(responseData, nextCursor, hasNext);
  }
}
