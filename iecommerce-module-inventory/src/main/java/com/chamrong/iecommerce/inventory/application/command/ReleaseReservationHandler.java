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
 * Releases a PENDING reservation back to available stock (order cancelled or cart expired).
 *
 * <p>The on-hand qty is NOT changed — only the reserved qty is decremented. Idempotent by {@code
 * referenceId}.
 *
 * <p>Structured log marker: {@code RESERVATION_RELEASED}
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReleaseReservationHandler {

  private static final String OP_TYPE = "RELEASE";

  private final ReservationPort reservations;
  private final OnHandProjectionPort projection;
  private final LedgerPort ledger;
  private final IdempotencyPort idempotency;
  private final ClockPort clock;

  public void handle(String tenantId, String referenceType, String referenceId, String actorId) {
    // Idempotency guard
    if (idempotency.check(tenantId, OP_TYPE, referenceId).isPresent()) {
      log.info(
          "[RESERVATION_RELEASED] DUPLICATE referenceId={} tenantId={} — skipping",
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

    reservation.release(now); // FSM: PENDING → RELEASED
    reservations.save(reservation);

    // Decrement reserved qty from projection (on-hand unchanged)
    var item =
        projection
            .findForUpdate(tenantId, reservation.getProductId(), reservation.getWarehouseId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Projection missing for released reservation: " + referenceId));

    item.decrementReserved(reservation.getQty());
    projection.save(item);

    // Append ledger (positive delta = stock freed back to available)
    ledger.append(
        StockLedgerEntry.of(
            tenantId,
            reservation.getProductId(),
            reservation.getWarehouseId(),
            EntryType.RELEASE,
            reservation.getQty(), // positive: units freed
            referenceType,
            referenceId,
            actorId,
            null,
            now));

    idempotency.record(tenantId, OP_TYPE, referenceId, "");

    log.info(
        "[RESERVATION_RELEASED] productId={} warehouseId={} qty={} available={} referenceId={}",
        reservation.getProductId(),
        reservation.getWarehouseId(),
        reservation.getQty(),
        item.getAvailableQty(),
        referenceId);
  }
}
