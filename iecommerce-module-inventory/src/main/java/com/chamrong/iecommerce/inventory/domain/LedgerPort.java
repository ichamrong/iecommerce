package com.chamrong.iecommerce.inventory.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for appending to and reading from the stock ledger.
 *
 * <p>Implementors: {@code JpaLedgerAdapter} in {@code infrastructure/persistence}. The ledger is
 * append-only; no update or delete methods are exposed.
 */
public interface LedgerPort {

  /** Appends a single ledger entry. The entry must not have an id yet. */
  StockLedgerEntry append(StockLedgerEntry entry);

  /**
   * Cursor-paginated history for a product across all warehouses, sorted by {@code (created_at
   * DESC, id DESC)}.
   *
   * @param tenantId required tenant scope
   * @param productId product to query
   * @param warehouseId optional warehouse filter; null = all warehouses
   * @param afterCreatedAt cursor upper-bound; null = first page
   * @param afterId cursor tie-break; null = first page
   * @param limit max rows (callers should request limit+1 to detect hasNext)
   */
  List<StockLedgerEntry> findPage(
      String tenantId,
      Long productId,
      Long warehouseId,
      Instant afterCreatedAt,
      Long afterId,
      int limit);

  /**
   * Finds a ledger entry by its external reference, used to detect duplicate writes.
   *
   * @param referenceType type of the referencing entity (e.g. "ORDER")
   * @param referenceId external reference id
   * @param entryType the operation type
   */
  Optional<StockLedgerEntry> findByRef(
      String tenantId,
      String referenceType,
      String referenceId,
      StockLedgerEntry.EntryType entryType);
}
