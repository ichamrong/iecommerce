package com.chamrong.iecommerce.inventory.domain;

import java.util.List;
import java.util.Optional;

/**
 * Port for reading and updating the on-hand stock projection ({@link InventoryItem}).
 *
 * <p>The separation of "forUpdate" vs plain "find" is intentional: callers that need to mutate the
 * projection (reserve/commit/release) MUST use {@link #findForUpdate} to acquire the DB row lock.
 * Read-only callers use {@link #find}.
 */
public interface OnHandProjectionPort {

  /**
   * Acquires a pessimistic write lock on the projection row for the duration of the current
   * transaction. Callers MUST be inside a transaction; throws if not.
   *
   * @return the locked entity, or empty if not yet created
   */
  Optional<InventoryItem> findForUpdate(String tenantId, Long productId, Long warehouseId);

  /** Read-only lookup — no row lock. Safe for display/availability checks with Redis fallback. */
  Optional<InventoryItem> find(String tenantId, Long productId, Long warehouseId);

  /** Returns all warehouse projections for a product (no lock). */
  List<InventoryItem> findAllByProduct(String tenantId, Long productId);

  /** Persists changes (insert or update). */
  InventoryItem save(InventoryItem item);

  /**
   * Creates or loads the projection, initializing with zero qty if absent. Called at the start of
   * every write use case to guarantee the row exists before locking.
   */
  InventoryItem getOrCreate(String tenantId, Long productId, Long warehouseId);
}
