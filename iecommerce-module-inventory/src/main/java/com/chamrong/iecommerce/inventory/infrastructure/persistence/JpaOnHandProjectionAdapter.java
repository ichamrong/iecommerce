package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.InventoryItem;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link OnHandProjectionPort}.
 *
 * <p>The {@link #findForUpdate} method delegates to the Spring Data query annotated with {@link
 * jakarta.persistence.LockModeType#PESSIMISTIC_WRITE}, which generates {@code SELECT ... FOR
 * UPDATE} in PostgreSQL. This MUST be called within an active transaction.
 *
 * <p>The {@link #getOrCreate} method should be called before {@link #findForUpdate} if the row may
 * not yet exist — creating the row first and then locking avoids a {@code SELECT ... FOR UPDATE} on
 * a non-existent row (which would be a no-op in PostgreSQL).
 */
@Component
@RequiredArgsConstructor
public class JpaOnHandProjectionAdapter implements OnHandProjectionPort {

  private final SpringDataOnHandRepository jpaRepo;

  @Override
  public Optional<InventoryItem> findForUpdate(String tenantId, Long productId, Long warehouseId) {
    return jpaRepo.findForUpdate(tenantId, productId, warehouseId);
  }

  @Override
  public Optional<InventoryItem> find(String tenantId, Long productId, Long warehouseId) {
    return jpaRepo.findByProjection(tenantId, productId, warehouseId);
  }

  @Override
  public List<InventoryItem> findAllByProduct(String tenantId, Long productId) {
    return jpaRepo.findAllByProduct(tenantId, productId);
  }

  @Override
  public InventoryItem save(InventoryItem item) {
    return jpaRepo.save(item);
  }

  @Override
  public InventoryItem getOrCreate(String tenantId, Long productId, Long warehouseId) {
    return jpaRepo
        .findByProjection(tenantId, productId, warehouseId)
        .orElseGet(
            () -> {
              var item = InventoryItem.create(tenantId, productId, warehouseId);
              return jpaRepo.save(item);
            });
  }
}
