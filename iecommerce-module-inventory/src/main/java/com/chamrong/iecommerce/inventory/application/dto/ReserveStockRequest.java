package com.chamrong.iecommerce.inventory.application.dto;

import java.time.Instant;

/**
 * Request object for creating a stock reservation. Validated by the API layer before passing to
 * {@link com.chamrong.iecommerce.inventory.application.command.ReserveStockHandler}.
 *
 * @param tenantId required tenant scope
 * @param productId product to reserve
 * @param warehouseId target warehouse
 * @param qty units to reserve (must be > 0)
 * @param referenceType type of the referencing entity (e.g. "ORDER")
 * @param referenceId external reference id (orderId, lineItemId)
 * @param actorId user or system actor
 * @param expiresAt when the hold expires; null = never expires
 */
public record ReserveStockRequest(
    String tenantId,
    Long productId,
    Long warehouseId,
    int qty,
    String referenceType,
    String referenceId,
    String actorId,
    Instant expiresAt) {}
