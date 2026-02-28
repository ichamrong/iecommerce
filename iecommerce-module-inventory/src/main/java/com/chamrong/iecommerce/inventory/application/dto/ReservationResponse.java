package com.chamrong.iecommerce.inventory.application.dto;

import com.chamrong.iecommerce.inventory.domain.StockReservation.ReservationStatus;
import java.time.Instant;

/**
 * Read model for a stock reservation — safe to expose via API.
 *
 * @param id reservation id
 * @param productId product
 * @param warehouseId warehouse
 * @param qty reserved quantity
 * @param referenceType external entity type (e.g. "ORDER")
 * @param referenceId external entity id
 * @param status PENDING / COMMITTED / RELEASED / EXPIRED
 * @param expiresAt expiry timestamp; null if no expiry
 * @param createdAt creation timestamp
 */
public record ReservationResponse(
    Long id,
    Long productId,
    Long warehouseId,
    int qty,
    String referenceType,
    String referenceId,
    ReservationStatus status,
    Instant expiresAt,
    Instant createdAt) {}
