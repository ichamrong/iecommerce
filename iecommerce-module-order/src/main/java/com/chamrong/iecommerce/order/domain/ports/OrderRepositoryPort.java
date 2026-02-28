package com.chamrong.iecommerce.order.domain.ports;

import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderState;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link Order} aggregate.
 *
 * <p>All listing methods return at most {@code limit + 1} rows to support keyset pagination.
 * Callers must slice the extra row off and encode the cursor from the last <em>included</em> row.
 *
 * <p>Domain boundary: only pure domain types cross this interface.
 */
public interface OrderRepositoryPort {

  /** Loads an order for mutation — DOES NOT acquire a DB lock. */
  Optional<Order> findById(Long id);

  /**
   * Loads an order with a pessimistic write lock ({@code SELECT … FOR UPDATE}). Must be called
   * inside an active transaction.
   */
  Optional<Order> findByIdForUpdate(Long id);

  Order save(Order order);

  // ── Cursor-paginated listing ───────────────────────────────────────────────

  /**
   * First page — no cursor. Sorted {@code (created_at DESC, id DESC)}. Returns up to {@code
   * fetchSize} rows (fetchSize = pageSize + 1 to detect hasNext).
   */
  List<Order> findByCustomerFirstPage(String tenantId, Long customerId, int fetchSize);

  /** Next page from cursor position. */
  List<Order> findByCustomerNextPage(
      String tenantId, Long customerId, Instant cursorTs, Long cursorId, int fetchSize);

  /** First page filtered by state. */
  List<Order> findByStateFirstPage(String tenantId, OrderState state, int fetchSize);

  /** Next page filtered by state. */
  List<Order> findByStateNextPage(
      String tenantId, OrderState state, Instant cursorTs, Long cursorId, int fetchSize);

  /** First page — all orders for tenant (used in tenant-level dashboards). */
  List<Order> findByTenantFirstPage(String tenantId, int fetchSize);

  /** Next page — all orders for tenant. */
  List<Order> findByTenantNextPage(String tenantId, Instant cursorTs, Long cursorId, int fetchSize);
}
