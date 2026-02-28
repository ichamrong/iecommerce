package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA backing repository for {@link StockLedgerEntry}.
 *
 * <p>All keyset queries sort by {@code (created_at DESC, id DESC)} and are backed by {@code
 * idx_ledger_cursor(product_id, created_at DESC, id DESC)} from the v17 migration.
 */
@Repository
interface SpringDataLedgerRepository extends JpaRepository<StockLedgerEntry, Long> {

  // ── First page (no cursor) ───────────────────────────────────────────────

  @Query(
      "SELECT e FROM StockLedgerEntry e WHERE e.tenantId = :tenantId AND e.productId = :productId"
          + " ORDER BY e.createdAt DESC, e.id DESC")
  List<StockLedgerEntry> findFirstPage(
      @Param("tenantId") String tenantId, @Param("productId") Long productId, Pageable pageable);

  @Query(
      "SELECT e FROM StockLedgerEntry e WHERE e.tenantId = :tenantId AND e.productId = :productId"
          + " AND e.warehouseId = :warehouseId ORDER BY e.createdAt DESC, e.id DESC")
  List<StockLedgerEntry> findFirstPageByWarehouse(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      Pageable pageable);

  // ── Subsequent pages (cursor) ────────────────────────────────────────────

  @Query(
      """
      SELECT e FROM StockLedgerEntry e
      WHERE e.tenantId = :tenantId AND e.productId = :productId
        AND (e.createdAt < :afterCreatedAt
             OR (e.createdAt = :afterCreatedAt AND e.id < :afterId))
      ORDER BY e.createdAt DESC, e.id DESC
      """)
  List<StockLedgerEntry> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  @Query(
      """
      SELECT e FROM StockLedgerEntry e
      WHERE e.tenantId = :tenantId AND e.productId = :productId
        AND e.warehouseId = :warehouseId
        AND (e.createdAt < :afterCreatedAt
             OR (e.createdAt = :afterCreatedAt AND e.id < :afterId))
      ORDER BY e.createdAt DESC, e.id DESC
      """)
  List<StockLedgerEntry> findNextPageByWarehouse(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  // ── Reference lookup (idempotency) ───────────────────────────────────────

  @Query(
      "SELECT e FROM StockLedgerEntry e WHERE e.tenantId = :tenantId"
          + " AND e.referenceType = :referenceType AND e.referenceId = :referenceId"
          + " AND e.entryType = :entryType")
  Optional<StockLedgerEntry> findByRef(
      @Param("tenantId") String tenantId,
      @Param("referenceType") String referenceType,
      @Param("referenceId") String referenceId,
      @Param("entryType") EntryType entryType);
}
