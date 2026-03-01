package com.chamrong.iecommerce.sale.domain.ports;

import java.math.BigDecimal;

public interface InventoryPort {
  void reserveStock(String productId, BigDecimal quantity, String tenantId);

  void releaseStock(String productId, BigDecimal quantity, String tenantId);

  void restock(String productId, BigDecimal quantity, String tenantId, String reason);
}
