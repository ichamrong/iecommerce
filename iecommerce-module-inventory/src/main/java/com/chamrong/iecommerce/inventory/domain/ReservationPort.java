package com.chamrong.iecommerce.inventory.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for persisting and querying {@link StockReservation} entities. */
public interface ReservationPort {

  StockReservation save(StockReservation reservation);

  /** Finds a reservation by its idempotency ref (tenant + referenceType + referenceId). */
  Optional<StockReservation> findByRef(String tenantId, String referenceType, String referenceId);

  /**
   * Returns all PENDING reservations whose {@code expires_at} is before {@code now}. Used by the
   * expiry scheduler.
   *
   * @param now current time
   * @param limit max batch size
   */
  List<StockReservation> findExpiredBefore(Instant now, int limit);

  /**
   * Cursor-paginated reservation history for a product.
   *
   * @param tenantId required tenant scope
   * @param productId product to query
   * @param status optional status filter; null = all statuses
   * @param afterCreatedAt cursor; null = first page
   * @param afterId cursor tie-break; null = first page
   * @param limit max rows
   */
  List<StockReservation> findPage(
      String tenantId,
      Long productId,
      StockReservation.ReservationStatus status,
      Instant afterCreatedAt,
      Long afterId,
      int limit);
}
