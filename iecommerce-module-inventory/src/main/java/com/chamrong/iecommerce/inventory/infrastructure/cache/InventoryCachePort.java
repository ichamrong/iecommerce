package com.chamrong.iecommerce.inventory.infrastructure.cache;

import com.chamrong.iecommerce.inventory.application.dto.OnHandResponse;
import java.util.Optional;

/**
 * Cache port for inventory read-side (on-hand availability).
 *
 * <p>ONLY on-hand reads are cached — never ledger entries or reservation writes. The risk of
 * serving stale stock levels is bounded by the configured TTL (default: 30s). This is acceptable
 * for storefront display but NOT for reservation validation (always hits DB).
 */
public interface InventoryCachePort {

  /** Returns cached on-hand for a product–warehouse pair, or empty on cache miss. */
  Optional<OnHandResponse> getOnHand(Long productId, Long warehouseId);

  /** Puts the on-hand read model into cache. */
  void putOnHand(Long productId, Long warehouseId, OnHandResponse response);

  /**
   * Evicts the on-hand entry — called every time the projection is mutated (receive, adjust,
   * commit, release, expire).
   */
  void evictOnHand(Long productId, Long warehouseId);
}
