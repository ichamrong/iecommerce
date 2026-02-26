package com.chamrong.iecommerce.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository {
  StockLevel save(StockLevel stockLevel);

  Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

  List<StockLevel> findByProductId(Long productId);

  List<StockLevel> findForUpdateByProductId(Long productId);

  List<StockLevel> findByWarehouseId(Long warehouseId);

  List<StockLevel> findByTenantIdAndQuantityLessThanEqual(String tenantId, int threshold);
}
