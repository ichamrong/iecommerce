package com.chamrong.iecommerce.inventory.domain;

import java.util.List;

public interface StockMovementRepository {
  StockMovement save(StockMovement movement);

  List<StockMovement> findByProductId(Long productId);

  List<StockMovement> findByWarehouseId(Long warehouseId);
}
