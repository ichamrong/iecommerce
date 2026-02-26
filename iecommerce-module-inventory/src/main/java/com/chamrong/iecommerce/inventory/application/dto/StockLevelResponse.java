package com.chamrong.iecommerce.inventory.application.dto;

public record StockLevelResponse(Long id, Long productId, Long warehouseId, Integer quantity) {}
