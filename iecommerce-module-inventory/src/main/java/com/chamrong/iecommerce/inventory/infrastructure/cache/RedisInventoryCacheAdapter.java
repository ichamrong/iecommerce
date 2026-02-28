package com.chamrong.iecommerce.inventory.infrastructure.cache;

import com.chamrong.iecommerce.inventory.application.dto.OnHandResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis implementation of {@link InventoryCachePort}.
 *
 * <p>Key schema: {@code inventory:available:{productId}:{warehouseId}} TTL: 30 seconds — short
 * enough to reflect near-real-time stock changes.
 *
 * <p>Why 30s? Stock levels change with every reservation/commit/release. A 30s window means at most
 * a 30s delay before the display reflects a change, which is acceptable for storefront reads.
 * Reservation validation NEVER reads from cache.
 *
 * <p>Activated only when a {@link RedisTemplate} bean is present in the context. Falls back to
 * {@link NoOpInventoryCacheAdapter} otherwise.
 */
@Slf4j
@Component
@ConditionalOnBean(RedisTemplate.class)
@RequiredArgsConstructor
public class RedisInventoryCacheAdapter implements InventoryCachePort {

  private static final Duration TTL = Duration.ofSeconds(30);
  private static final String PREFIX = "inventory:available:";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public Optional<OnHandResponse> getOnHand(Long productId, Long warehouseId) {
    try {
      String raw = redisTemplate.opsForValue().get(key(productId, warehouseId));
      if (raw == null) return Optional.empty();
      return Optional.of(objectMapper.readValue(raw, OnHandResponse.class));
    } catch (Exception ex) {
      log.warn(
          "[Inventory] Redis read failed for productId={} warehouseId={}: {}",
          productId,
          warehouseId,
          ex.getMessage());
      return Optional.empty(); // Fail-open: fall through to DB
    }
  }

  @Override
  public void putOnHand(Long productId, Long warehouseId, OnHandResponse response) {
    try {
      String value = objectMapper.writeValueAsString(response);
      redisTemplate.opsForValue().set(key(productId, warehouseId), value, TTL);
    } catch (Exception ex) {
      log.warn(
          "[Inventory] Redis write failed for productId={} warehouseId={}: {}",
          productId,
          warehouseId,
          ex.getMessage());
      // Fail-open: cache miss is acceptable; DB will serve the read
    }
  }

  @Override
  public void evictOnHand(Long productId, Long warehouseId) {
    try {
      redisTemplate.delete(key(productId, warehouseId));
    } catch (Exception ex) {
      log.warn(
          "[Inventory] Redis evict failed for productId={} warehouseId={}: {}",
          productId,
          warehouseId,
          ex.getMessage());
    }
  }

  private static String key(Long productId, Long warehouseId) {
    return PREFIX + productId + ":" + warehouseId;
  }
}
