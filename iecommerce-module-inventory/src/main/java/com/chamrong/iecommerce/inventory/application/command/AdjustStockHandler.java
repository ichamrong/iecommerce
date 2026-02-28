package com.chamrong.iecommerce.inventory.application.command;

import com.chamrong.iecommerce.inventory.domain.ClockPort;
import com.chamrong.iecommerce.inventory.domain.IdempotencyPort;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles manual stock adjustments (e.g., shrinkage, damage write-off, cycle count correction).
 *
 * <p>All adjustments are idempotent by {@code referenceId}. A valid {@code adjustmentReason} is
 * required so that every ledger entry carries a human-readable audit trail.
 *
 * <p>Negative deltas are allowed (shrinkage). The flag {@code allowNegativeResult} passed from the
 * caller controls whether the resulting on-hand qty may go below 0 (default: false for safety).
 *
 * <p>Structured log marker: {@code STOCK_ADJUSTED}
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdjustStockHandler {

  private static final String OP_TYPE = "ADJUST";

  private final OnHandProjectionPort projection;
  private final LedgerPort ledger;
  private final IdempotencyPort idempotency;
  private final ClockPort clock;

  /**
   * Applies a stock delta to a product–warehouse projection.
   *
   * @param tenantId tenant scope
   * @param productId target product
   * @param warehouseId target warehouse
   * @param delta signed qty delta (positive = add, negative = remove)
   * @param referenceId idempotency key (e.g. adjustment note ID)
   * @param adjustmentReason human-readable reason (e.g. "DAMAGED", "CYCLE_COUNT")
   * @param actorId user performing the adjustment
   * @param allowNegative if true, on-hand may go below 0 (forced write-off)
   * @param metadata optional JSON
   */
  public void handle(
      String tenantId,
      Long productId,
      Long warehouseId,
      int delta,
      String referenceId,
      String adjustmentReason,
      String actorId,
      boolean allowNegative,
      String metadata) {

    if (delta == 0) throw new IllegalArgumentException("Adjustment delta must be non-zero");

    // Idempotency check
    if (idempotency.check(tenantId, OP_TYPE, referenceId).isPresent()) {
      log.info(
          "[STOCK_ADJUSTED] DUPLICATE referenceId={} tenantId={} productId={} — skipping",
          referenceId,
          tenantId,
          productId);
      return;
    }

    var now = clock.now();
    var item = projection.getOrCreate(tenantId, productId, warehouseId);
    int before = item.getOnHandQty();
    item.applyAdjustment(delta, allowNegative);
    projection.save(item);

    ledger.append(
        StockLedgerEntry.of(
            tenantId,
            productId,
            warehouseId,
            EntryType.ADJUST,
            delta,
            "ADJUSTMENT",
            referenceId,
            actorId,
            metadata,
            now));

    idempotency.record(tenantId, OP_TYPE, referenceId, "");

    log.info(
        "[STOCK_ADJUSTED] productId={} warehouseId={} delta={} before={} after={} reason={}"
            + " actor={}",
        productId,
        warehouseId,
        delta,
        before,
        item.getOnHandQty(),
        adjustmentReason,
        actorId);
  }
}
