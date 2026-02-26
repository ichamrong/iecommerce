package com.chamrong.iecommerce.inventory.application.dto;

import com.chamrong.iecommerce.inventory.domain.StockMovement.MovementReason;

/** Request to adjust stock for a single product-warehouse combination. */
public record AdjustStockRequest(
    Long productId, Long warehouseId, int delta, MovementReason reason, String comment) {}
