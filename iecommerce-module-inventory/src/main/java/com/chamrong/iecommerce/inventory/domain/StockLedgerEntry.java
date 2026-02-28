package com.chamrong.iecommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

/**
 * Append-only ledger entry for every inventory change.
 *
 * <p>This IS the source of truth. The {@link InventoryItem} projection is derived from these
 * entries. All columns carry {@code updatable=false} to make the immutability contract enforceable
 * at the JPA layer — any attempt to update a ledger row will throw an exception.
 *
 * <p>Table: {@code inventory_stock_ledger}
 *
 * <p>Indexes (see Liquibase migration v17):
 *
 * <ul>
 *   <li>{@code idx_ledger_cursor(product_id, created_at DESC, id DESC)} — history pagination
 *   <li>{@code idx_ledger_ref(reference_type, reference_id)} — idempotency lookups
 *   <li>{@code idx_ledger_tenant_product(tenant_id, product_id)} — tenant-scoped list
 * </ul>
 */
@Getter
@Entity
@Table(name = "inventory_stock_ledger")
public class StockLedgerEntry {

  public enum EntryType {
    RECEIVE, // inbound stock receipt
    ADJUST, // manual correction / write-off
    RESERVE, // units held for order
    COMMIT, // sale deducted (on-hand AND reservation cleared)
    RELEASE, // reservation cancelled (reservation cleared, on-hand unchanged)
    EXPIRE, // reservation expired (reservation cleared, on-hand unchanged)
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
  private String tenantId;

  @Column(name = "product_id", nullable = false, updatable = false)
  private Long productId;

  @Column(name = "warehouse_id", nullable = false, updatable = false)
  private Long warehouseId;

  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false, updatable = false, length = 20)
  private EntryType entryType;

  /** Signed delta applied to on-hand qty (positive=add, negative=deduct). */
  @Column(name = "qty_delta", nullable = false, updatable = false)
  private int qtyDelta;

  /** Type of the referencing entity (e.g. {@code "ORDER"}, {@code "ADJUSTMENT"}). */
  @Column(name = "reference_type", updatable = false, length = 100)
  private String referenceType;

  /**
   * External reference ID (e.g. orderId, adjustmentId). Together with {@code referenceType} and
   * {@code entryType} this forms the idempotency key for ledger writes.
   */
  @Column(name = "reference_id", updatable = false, length = 255)
  private String referenceId;

  /** Actor who triggered the operation (userId or system identifier). */
  @Column(name = "actor_id", updatable = false, length = 255)
  private String actorId;

  /** Optional JSON metadata (e.g. warehouse notes, adjustment reason). */
  @Column(name = "metadata", updatable = false, columnDefinition = "TEXT")
  private String metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  // ── Factory ──────────────────────────────────────────────────────────────

  private StockLedgerEntry() {}

  public static StockLedgerEntry of(
      String tenantId,
      Long productId,
      Long warehouseId,
      EntryType entryType,
      int qtyDelta,
      String referenceType,
      String referenceId,
      String actorId,
      String metadata,
      Instant now) {
    var e = new StockLedgerEntry();
    e.tenantId = tenantId;
    e.productId = productId;
    e.warehouseId = warehouseId;
    e.entryType = entryType;
    e.qtyDelta = qtyDelta;
    e.referenceType = referenceType;
    e.referenceId = referenceId;
    e.actorId = actorId;
    e.metadata = metadata;
    e.createdAt = now;
    return e;
  }
}
