package com.chamrong.iecommerce.inventory.application.command;

import com.chamrong.iecommerce.inventory.domain.ClockPort;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.ReservationPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import com.chamrong.iecommerce.inventory.domain.StockReservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler that processes expired reservations in batches.
 *
 * <p>Each expired reservation triggers:
 *
 * <ol>
 *   <li>FSM transition PENDING → EXPIRED
 *   <li>Projection reserved qty decremented (stock freed)
 *   <li>Ledger entry of type EXPIRE appended
 * </ol>
 *
 * <p>Runs every 60 seconds. Each run processes up to {@code BATCH_SIZE} expired reservations. For
 * very high volumes, this can be evolved to a Kafka consumer or a DB-backed task queue.
 *
 * <p>Structured log marker: {@code RESERVATION_EXPIRED}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpireReservationsHandler {

  private static final int BATCH_SIZE = 100;

  private final ReservationPort reservations;
  private final OnHandProjectionPort projection;
  private final LedgerPort ledger;
  private final ClockPort clock;

  /**
   * Scheduled expiry run. Each reservation is expired in its own transaction to avoid one failure
   * rolling back the entire batch.
   */
  @Scheduled(fixedDelayString = "${inventory.expiry.interval-ms:60000}")
  public void run() {
    var now = clock.now();
    var expired = reservations.findExpiredBefore(now, BATCH_SIZE);

    if (expired.isEmpty()) return;

    log.info("[RESERVATION_EXPIRED] Processing {} expired reservations", expired.size());

    for (var r : expired) {
      expireOne(r);
    }
  }

  @Transactional
  public void expireOne(StockReservation reservation) {
    try {
      var now = clock.now();
      reservation.expire(now);
      reservations.save(reservation);

      projection
          .findForUpdate(
              reservation.getTenantId(), reservation.getProductId(), reservation.getWarehouseId())
          .ifPresent(
              item -> {
                item.decrementReserved(reservation.getQty());
                projection.save(item);
              });

      ledger.append(
          StockLedgerEntry.of(
              reservation.getTenantId(),
              reservation.getProductId(),
              reservation.getWarehouseId(),
              EntryType.EXPIRE,
              reservation.getQty(),
              reservation.getReferenceType(),
              reservation.getReferenceId(),
              "SYSTEM",
              null,
              now));

      log.info(
          "[RESERVATION_EXPIRED] reservationId={} productId={} qty={} referenceId={}",
          reservation.getId(),
          reservation.getProductId(),
          reservation.getQty(),
          reservation.getReferenceId());

    } catch (Exception ex) {
      log.error(
          "[RESERVATION_EXPIRED] FAILED to expire reservationId={}: {}",
          reservation.getId(),
          ex.getMessage(),
          ex);
    }
  }
}
