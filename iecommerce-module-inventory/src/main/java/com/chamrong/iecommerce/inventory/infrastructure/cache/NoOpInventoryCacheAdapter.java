package com.chamrong.iecommerce.inventory.infrastructure.cache;

import com.chamrong.iecommerce.inventory.application.dto.OnHandResponse;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op fallback for {@link InventoryCachePort} used when Redis is not configured.
 *
 * <p>Activated via {@code @ConditionalOnMissingBean(InventoryCachePort.class)} — meaning it
 * registers only when {@link RedisInventoryCacheAdapter} is NOT registered (i.e., when {@code
 * RedisTemplate} bean is absent). This requires no environment variable or explicit config.
 *
 * <p>All reads return {@link Optional#empty()} (cache miss → DB fallback). All writes and evictions
 * are silent no-ops.
 */
@Component
@ConditionalOnMissingBean(InventoryCachePort.class)
public class NoOpInventoryCacheAdapter implements InventoryCachePort {

  @Override
  public Optional<OnHandResponse> getOnHand(Long productId, Long warehouseId) {
    return Optional.empty();
  }

  @Override
  public void putOnHand(Long productId, Long warehouseId, OnHandResponse response) {
    // no-op
  }

  @Override
  public void evictOnHand(Long productId, Long warehouseId) {
    // no-op
  }
}
