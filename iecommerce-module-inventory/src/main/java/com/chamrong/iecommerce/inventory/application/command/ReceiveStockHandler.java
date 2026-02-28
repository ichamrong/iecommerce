package com.chamrong.iecommerce.inventory.application.command;

import com.chamrong.iecommerce.inventory.domain.ClockPort;
import com.chamrong.iecommerce.inventory.domain.IdempotencyPort;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles inbound stock receipts (deliveries, returns from supplier).
 *
 * <p>Idempotent by {@code referenceId}: a second call with the same referenceId returns silently
 * without re-applying the receipt. The idempotency guard and the projection update occur within a
 * single transaction.
 *
 * <p>Structured log marker: {@code STOCK_RECEIVED}
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReceiveStockHandler {

  private static final String OP_TYPE = "RECEIVE";

  private final OnHandProjectionPort projection;
  private final LedgerPort ledger;
  private final IdempotencyPort idempotency;
  private final ClockPort clock;
  private final MeterRegistry meterRegistry;

  private Counter receiveCounter;

  @PostConstruct
  void init() {
    receiveCounter =
        Counter.builder("inventory.stock.received")
            .description("Total units received into inventory")
            .register(meterRegistry);
  }

  /**
   * Receives stock into a warehouse.
   *
   * @param tenantId tenant scope
   * @param productId product receiving stock
   * @param warehouseId target warehouse
   * @param qty units received (must be positive)
   * @param referenceId idempotency key (e.g. purchase order id)
   * @param actorId user or system performing the operation
   * @param metadata optional JSON notes
   */
  public void handle(
      String tenantId,
      Long productId,
      Long warehouseId,
      int qty,
      String referenceId,
      String actorId,
      String metadata) {

    if (qty <= 0) throw new IllegalArgumentException("Receipt qty must be positive, got: " + qty);

    // Idempotency check
    if (idempotency.check(tenantId, OP_TYPE, referenceId).isPresent()) {
      log.info(
          "[STOCK_RECEIVED] DUPLICATE referenceId={} tenantId={} productId={} — skipping",
          referenceId,
          tenantId,
          productId);
      return;
    }

    var now = clock.now();
    var item = projection.getOrCreate(tenantId, productId, warehouseId);
    item.applyReceipt(qty);
    projection.save(item);

    ledger.append(
        StockLedgerEntry.of(
            tenantId,
            productId,
            warehouseId,
            EntryType.RECEIVE,
            qty,
            "RECEIVE",
            referenceId,
            actorId,
            metadata,
            now));

    idempotency.record(tenantId, OP_TYPE, referenceId, "");

    receiveCounter.increment(qty);

    log.info(
        "[STOCK_RECEIVED] productId={} warehouseId={} qty={} onHand={} referenceId={} actor={}",
        productId,
        warehouseId,
        qty,
        item.getOnHandQty(),
        referenceId,
        actorId);
  }
}
