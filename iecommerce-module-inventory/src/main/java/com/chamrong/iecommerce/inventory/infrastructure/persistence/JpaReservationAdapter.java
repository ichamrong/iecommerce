package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.ReservationPort;
import com.chamrong.iecommerce.inventory.domain.StockReservation;
import com.chamrong.iecommerce.inventory.domain.StockReservation.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/** JPA adapter implementing {@link ReservationPort}. */
@Component
@RequiredArgsConstructor
public class JpaReservationAdapter implements ReservationPort {

  private final SpringDataReservationRepository jpaRepo;

  @Override
  public StockReservation save(StockReservation reservation) {
    return jpaRepo.save(reservation);
  }

  @Override
  public Optional<StockReservation> findByRef(
      String tenantId, String referenceType, String referenceId) {
    return jpaRepo.findByRef(tenantId, referenceType, referenceId);
  }

  @Override
  public List<StockReservation> findExpiredBefore(Instant now, int limit) {
    return jpaRepo.findExpiredBefore(now, PageRequest.of(0, limit));
  }

  @Override
  public List<StockReservation> findPage(
      String tenantId,
      Long productId,
      ReservationStatus status,
      Instant afterCreatedAt,
      Long afterId,
      int limit) {
    var pageable = PageRequest.of(0, limit);

    if (afterCreatedAt == null || afterId == null) {
      return status == null
          ? jpaRepo.findFirstPage(tenantId, productId, pageable)
          : jpaRepo.findFirstPageByStatus(tenantId, productId, status, pageable);
    }
    return status == null
        ? jpaRepo.findNextPage(tenantId, productId, afterCreatedAt, afterId, pageable)
        : jpaRepo.findNextPageByStatus(
            tenantId, productId, status, afterCreatedAt, afterId, pageable);
  }
}
