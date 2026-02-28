package com.chamrong.iecommerce.inventory.application.command;

import com.chamrong.iecommerce.inventory.application.dto.ReserveStockRequest;
import com.chamrong.iecommerce.inventory.domain.ClockPort;
import com.chamrong.iecommerce.inventory.domain.IdempotencyPort;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.OutOfStockException;
import com.chamrong.iecommerce.inventory.domain.ReservationPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import com.chamrong.iecommerce.inventory.domain.StockReservation;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles stock reservation for an order line or cart hold.
 *
 * <h2>Concurrency Strategy</h2>
 *
 * <ol>
 *   <li>Idempotency check — prevents duplicate reserves for the same external reference.
 *   <li>{@link OnHandProjectionPort#findForUpdate} issues {@code SELECT ... FOR UPDATE}, acquiring
 *       a pessimistic row-level lock. This prevents two concurrent threads from both reading 10
 *       available and both succeeding when only 10 exist.
 *   <li>{@link com.chamrong.iecommerce.inventory.domain.InventoryItem#incrementReserved} validates
 *       the available qty <em>after</em> the lock is held — no TOCTOU race.
 *   <li>{@link ReservationPort#save} persists the reservation record.
 *   <li>{@link LedgerPort#append} records the ledger entry.
 *   <li>{@link IdempotencyPort#record} persists the idempotency key — all within one transaction.
 * </ol>
 *
 * <p>Structured log marker: {@code STOCK_RESERVED}
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReserveStockHandler {

  private static final String OP_TYPE = "RESERVE";

  private final OnHandProjectionPort projection;
  private final ReservationPort reservations;
  private final LedgerPort ledger;
  private final IdempotencyPort idempotency;
  private final ClockPort clock;
  private final MeterRegistry meterRegistry;

  private Counter successCounter;
  private Counter failCounter;
  private Timer reserveTimer;

  @PostConstruct
  void init() {
    successCounter =
        Counter.builder("inventory.reservation.created")
            .description("Successful reservation count")
            .register(meterRegistry);
    failCounter =
        Counter.builder("inventory.reservation.failed")
            .description("Failed reservation count (out-of-stock)")
            .register(meterRegistry);
    reserveTimer =
        Timer.builder("inventory.reserve.latency")
            .description("Time taken to acquire a reservation")
            .register(meterRegistry);
  }

  /**
   * Reserves stock for a single product–warehouse combination.
   *
   * @param req fully validated reservation request
   * @return the created {@link StockReservation}
   * @throws OutOfStockException if available qty is insufficient after acquiring the row lock
   */
  public StockReservation handle(ReserveStockRequest req) {
    return reserveTimer.record(
        () -> {
          // 1. Idempotency guard
          var existing =
              reservations.findByRef(req.tenantId(), req.referenceType(), req.referenceId());
          if (existing.isPresent()) {
            log.info(
                "[STOCK_RESERVED] DUPLICATE referenceId={} tenantId={} productId={} — returning"
                    + " existing",
                req.referenceId(),
                req.tenantId(),
                req.productId());
            return existing.get();
          }

          var now = clock.now();

          // 2. Lock-then-validate — pessimistic row lock prevents race conditions
          var item =
              projection
                  .findForUpdate(req.tenantId(), req.productId(), req.warehouseId())
                  .orElseThrow(() -> new OutOfStockException(req.productId(), req.qty(), 0));

          item.incrementReserved(req.qty()); // throws OutOfStockException if insufficient
          projection.save(item);

          // 3. Persist reservation record
          var reservation =
              StockReservation.create(
                  req.tenantId(),
                  req.productId(),
                  req.warehouseId(),
                  req.qty(),
                  req.referenceType(),
                  req.referenceId(),
                  req.expiresAt(),
                  now);
          reservations.save(reservation);

          // 4. Append ledger
          ledger.append(
              StockLedgerEntry.of(
                  req.tenantId(),
                  req.productId(),
                  req.warehouseId(),
                  EntryType.RESERVE,
                  -req.qty(),
                  req.referenceType(),
                  req.referenceId(),
                  req.actorId(),
                  null,
                  now));

          // 5. Record idempotency key
          idempotency.record(req.tenantId(), OP_TYPE, req.referenceId(), "");

          successCounter.increment();

          log.info(
              "[STOCK_RESERVED] productId={} warehouseId={} qty={} available={} referenceId={}"
                  + " expiresAt={}",
              req.productId(),
              req.warehouseId(),
              req.qty(),
              item.getAvailableQty(),
              req.referenceId(),
              req.expiresAt());

          return reservation;
        });
  }
}
