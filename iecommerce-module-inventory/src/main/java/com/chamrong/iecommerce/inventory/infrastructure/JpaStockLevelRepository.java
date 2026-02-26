package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.StockLevel;
import com.chamrong.iecommerce.inventory.domain.StockLevelRepository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link StockLevelRepository} port. */
@Repository
public interface JpaStockLevelRepository
    extends JpaRepository<StockLevel, Long>, StockLevelRepository {

  @Override
  Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

  @Override
  List<StockLevel> findByProductId(Long productId);

  @Override
  List<StockLevel> findByWarehouseId(Long warehouseId);

  @Override
  List<StockLevel> findByTenantIdAndQuantityLessThanEqual(String tenantId, int quantity);

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<StockLevel> findForUpdateByProductId(Long productId);
}
