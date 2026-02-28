package com.chamrong.iecommerce.inventory.application.dto;

/**
 * Represents the on-hand stock level for a single product–warehouse pair.
 *
 * @param productId product identifier
 * @param warehouseId warehouse identifier
 * @param onHandQty physical units on-hand
 * @param reservedQty units currently held by open reservations
 * @param availableQty units available for new reservations (on-hand − reserved)
 */
public record OnHandResponse(
    Long productId, Long warehouseId, int onHandQty, int reservedQty, int availableQty) {}
