package com.chamrong.iecommerce.inventory;

/**
 * Public API of the Inventory module. Provides methods for stock reservation and availability
 * checks.
 */
public interface InventoryApi {

  /**
   * Reserves stock for a given product and quantity. Finds the first warehouse with enough
   * available stock.
   */
  void reserveStock(String tenantId, Long productId, int quantity);

  /** Releases previously reserved stock (e.g., when order is cancelled). */
  void releaseStock(String tenantId, Long productId, int quantity);

  /**
   * Deducts stock (physically removed) and matching reservation. Called when an order is shipped.
   */
  void deductStock(String tenantId, Long productId, int quantity);

  /**
   * Instantly deducts stock for POS without picking/packing reservation. Decrements available
   * quantity directly.
   */
  void deductPosSaleStock(String tenantId, Long productId, int quantity, Long terminalId);
}
