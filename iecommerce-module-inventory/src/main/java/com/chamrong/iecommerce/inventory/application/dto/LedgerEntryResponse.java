package com.chamrong.iecommerce.inventory.application.dto;

import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry.EntryType;
import java.time.Instant;

/**
 * Read model for a single ledger entry — safe to expose via API.
 *
 * @param id ledger entry id
 * @param productId product
 * @param warehouseId warehouse
 * @param entryType RECEIVE / ADJUST / RESERVE / COMMIT / RELEASE / EXPIRE
 * @param qtyDelta signed qty change
 * @param referenceType external entity type
 * @param referenceId external entity id
 * @param actorId actor who triggered the operation
 * @param createdAt ledger entry timestamp
 */
public record LedgerEntryResponse(
    Long id,
    Long productId,
    Long warehouseId,
    EntryType entryType,
    int qtyDelta,
    String referenceType,
    String referenceId,
    String actorId,
    Instant createdAt) {}
