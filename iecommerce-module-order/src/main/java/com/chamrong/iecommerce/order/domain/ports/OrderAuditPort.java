package com.chamrong.iecommerce.order.domain.ports;

import com.chamrong.iecommerce.order.domain.OrderAuditLog;
import com.chamrong.iecommerce.order.domain.OrderState;
import java.time.Instant;
import java.util.List;

/** Port for order audit log persistence. */
public interface OrderAuditPort {

  /**
   * Appends an audit entry for an order state transition.
   *
   * @param orderId order id
   * @param tenantId tenant id
   * @param from previous state (null for creation)
   * @param to new state
   * @param action action name
   * @param performedBy actor
   * @param context optional context (null allowed)
   */
  void log(
      Long orderId,
      String tenantId,
      OrderState from,
      OrderState to,
      String action,
      String performedBy,
      String context);

  /**
   * First page of audit entries for an order (keyset pagination).
   *
   * @param orderId order id
   * @param limit page size
   * @return entries ordered by occurredAt DESC, id DESC
   */
  List<OrderAuditLog> findByOrderFirstPage(Long orderId, int limit);

  /**
   * Next page of audit entries after the given cursor.
   *
   * @param orderId order id
   * @param occurredAt cursor timestamp
   * @param id cursor id
   * @param limit page size
   * @return entries ordered by occurredAt DESC, id DESC
   */
  List<OrderAuditLog> findByOrderNextPage(Long orderId, Instant occurredAt, Long id, int limit);
}
