package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link LedgerPort}.
 *
 * <p>Delegates to {@link SpringDataLedgerRepository}. Cursor pagination uses keyset semantics on
 * {@code (created_at DESC, id DESC)} — O(log N) seek backed by {@code idx_ledger_cursor}.
 */
@Component
@RequiredArgsConstructor
public class JpaLedgerAdapter implements LedgerPort {

  private final SpringDataLedgerRepository jpaRepo;

  @Override
  public StockLedgerEntry append(StockLedgerEntry entry) {
    return jpaRepo.save(entry);
  }

  @Override
  public List<StockLedgerEntry> findPage(
      String tenantId,
      Long productId,
      Long warehouseId,
      Instant afterCreatedAt,
      Long afterId,
      int limit) {
    var pageable = PageRequest.of(0, limit);

    if (afterCreatedAt == null || afterId == null) {
      return warehouseId == null
          ? jpaRepo.findFirstPage(tenantId, productId, pageable)
          : jpaRepo.findFirstPageByWarehouse(tenantId, productId, warehouseId, pageable);
    }
    return warehouseId == null
        ? jpaRepo.findNextPage(tenantId, productId, afterCreatedAt, afterId, pageable)
        : jpaRepo.findNextPageByWarehouse(
            tenantId, productId, warehouseId, afterCreatedAt, afterId, pageable);
  }

  @Override
  public Optional<StockLedgerEntry> findByRef(
      String tenantId, String referenceType, String referenceId, EntryType entryType) {
    return jpaRepo.findByRef(tenantId, referenceType, referenceId, entryType);
  }
}
