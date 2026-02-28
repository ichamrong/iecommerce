package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.StockReservation;
import com.chamrong.iecommerce.inventory.domain.StockReservation.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA backing repository for {@link StockReservation}.
 *
 * <p>Keyset queries sort by {@code (created_at DESC, id DESC)}, backed by {@code
 * idx_reservation_cursor} from migration v17.
 */
@Repository
interface SpringDataReservationRepository extends JpaRepository<StockReservation, Long> {

  @Query(
      "SELECT r FROM StockReservation r WHERE r.tenantId = :tenantId"
          + " AND r.referenceType = :referenceType AND r.referenceId = :referenceId")
  Optional<StockReservation> findByRef(
      @Param("tenantId") String tenantId,
      @Param("referenceType") String referenceType,
      @Param("referenceId") String referenceId);

  /** Finds PENDING reservations past their expiry for batch expiry processing. */
  @Query(
      "SELECT r FROM StockReservation r WHERE r.status = 'PENDING'"
          + " AND r.expiresAt IS NOT NULL AND r.expiresAt < :now")
  List<StockReservation> findExpiredBefore(@Param("now") Instant now, Pageable pageable);

  // ── First-page keyset ────────────────────────────────────────────────────

  @Query(
      "SELECT r FROM StockReservation r WHERE r.tenantId = :tenantId AND r.productId = :productId"
          + " ORDER BY r.createdAt DESC, r.id DESC")
  List<StockReservation> findFirstPage(
      @Param("tenantId") String tenantId, @Param("productId") Long productId, Pageable pageable);

  @Query(
      "SELECT r FROM StockReservation r WHERE r.tenantId = :tenantId AND r.productId = :productId"
          + " AND r.status = :status ORDER BY r.createdAt DESC, r.id DESC")
  List<StockReservation> findFirstPageByStatus(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("status") ReservationStatus status,
      Pageable pageable);

  // ── Subsequent-page keyset ───────────────────────────────────────────────

  @Query(
      """
      SELECT r FROM StockReservation r
      WHERE r.tenantId = :tenantId AND r.productId = :productId
        AND (r.createdAt < :afterCreatedAt
             OR (r.createdAt = :afterCreatedAt AND r.id < :afterId))
      ORDER BY r.createdAt DESC, r.id DESC
      """)
  List<StockReservation> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  @Query(
      """
      SELECT r FROM StockReservation r
      WHERE r.tenantId = :tenantId AND r.productId = :productId AND r.status = :status
        AND (r.createdAt < :afterCreatedAt
             OR (r.createdAt = :afterCreatedAt AND r.id < :afterId))
      ORDER BY r.createdAt DESC, r.id DESC
      """)
  List<StockReservation> findNextPageByStatus(
      @Param("tenantId") String tenantId,
      @Param("productId") Long productId,
      @Param("status") ReservationStatus status,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);
}
