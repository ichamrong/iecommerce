package com.chamrong.iecommerce.inventory.application.command;

import com.chamrong.iecommerce.inventory.domain.ClockPort;
import com.chamrong.iecommerce.inventory.domain.IdempotencyPort;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.ReservationPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Commits a previously created reservation — physically deducts on-hand and clears reserved qty.
 *
 * <p>Called when an order is confirmed/paid. Idempotent by {@code referenceId}.
 *
 * <p>Structured log marker: {@code RESERVATION_COMMITTED}
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommitReservationHandler {

  private static final String OP_TYPE = "COMMIT";

  private final ReservationPort reservations;
  private final OnHandProjectionPort projection;
  private final LedgerPort ledger;
  private final IdempotencyPort idempotency;
  private final ClockPort clock;

  /**
   * Commits a reservation.
   *
   * @param tenantId tenant scope
   * @param referenceType e.g. "ORDER"
   * @param referenceId external reference (orderId)
   * @param actorId user or system actor
   */
  public void handle(String tenantId, String referenceType, String referenceId, String actorId) {
    // Idempotency guard
    if (idempotency.check(tenantId, OP_TYPE, referenceId).isPresent()) {
      log.info(
          "[RESERVATION_COMMITTED] DUPLICATE referenceId={} tenantId={} — skipping",
          referenceId,
          tenantId);
      return;
    }

    var now = clock.now();

    var reservation =
        reservations
            .findByRef(tenantId, referenceType, referenceId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Reservation not found: referenceType="
                            + referenceType
                            + " referenceId="
                            + referenceId));

    reservation.commit(now); // FSM: PENDING → COMMITTED (throws if wrong state)
    reservations.save(reservation);

    // Update projection — deduct both reservation and on-hand
    var item =
        projection
            .findForUpdate(tenantId, reservation.getProductId(), reservation.getWarehouseId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Projection missing for committed reservation: " + referenceId));

    item.commitReservation(reservation.getQty());
    projection.save(item);

    // Append ledger
    ledger.append(
        StockLedgerEntry.of(
            tenantId,
            reservation.getProductId(),
            reservation.getWarehouseId(),
            EntryType.COMMIT,
            -reservation.getQty(),
            referenceType,
            referenceId,
            actorId,
            null,
            now));

    idempotency.record(tenantId, OP_TYPE, referenceId, "");

    log.info(
        "[RESERVATION_COMMITTED] productId={} warehouseId={} qty={} onHand={} referenceId={}"
            + " actor={}",
        reservation.getProductId(),
        reservation.getWarehouseId(),
        reservation.getQty(),
        item.getOnHandQty(),
        referenceId,
        actorId);
  }
}
