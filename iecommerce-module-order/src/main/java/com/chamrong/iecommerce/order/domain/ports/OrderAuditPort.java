package com.chamrong.iecommerce.order.domain.ports;

import com.chamrong.iecommerce.order.domain.OrderAuditLog;
import com.chamrong.iecommerce.order.domain.OrderState;
import java.time.Instant;
import java.util.List;

/**
 * Port for writing and reading the immutable order audit log.
 *
 * <p>Audit entries are append-only (never updated or deleted). See {@link OrderAuditLog}.
 */
public interface OrderAuditPort {

  /** Appends an audit entry. Must be called in the same transaction as the Order save. */
  void log(
      Long orderId,
      String tenantId,
      OrderState from,
      OrderState to,
      String action,
      String performedBy,
      String context);

  // ── Cursor-paginated read ─────────────────────────────────────────────────

  /**
   * First page of audit history for an order. Sorted {@code (occurred_at DESC, id DESC)}.
   *
   * @param fetchSize = pageSize + 1 so the caller can detect hasNext
   */
  List<OrderAuditLog> findByOrderFirstPage(Long orderId, int fetchSize);

  /** Next page from cursor position. */
  List<OrderAuditLog> findByOrderNextPage(
      Long orderId, Instant cursorTs, Long cursorId, int fetchSize);
}
