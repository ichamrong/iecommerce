package com.chamrong.iecommerce.catalog.infrastructure.cache;

import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of {@link CatalogCachePort}.
 *
 * <p>Only activated when a {@link RedisTemplate} bean is present in the context. If Redis is not
 * configured, the no-op implementation {@link NoOpCatalogCacheAdapter} is used instead.
 *
 * <p>Key schema:
 *
 * <ul>
 *   <li>{@code catalog:product:{id}} — product by ID, TTL 10 min
 *   <li>{@code catalog:product:slug:{tenantId}:{slug}} — product by slug, TTL 10 min
 * </ul>
 */
@Component
@ConditionalOnBean(RedisTemplate.class)
@RequiredArgsConstructor
@Slf4j
public class RedisCatalogCacheAdapter implements CatalogCachePort {

  private static final Duration TTL = Duration.ofMinutes(10);
  private static final String PREFIX_ID = "catalog:product:";
  private static final String PREFIX_SLUG = "catalog:product:slug:";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  // ── By ID ─────────────────────────────────────────────────────────────────

  @Override
  public Optional<ProductResponse> getProduct(Long id) {
    return get(PREFIX_ID + id);
  }

  @Override
  public void putProduct(Long id, ProductResponse response) {
    put(PREFIX_ID + id, response);
  }

  @Override
  public void evictProduct(Long id) {
    redisTemplate.delete(PREFIX_ID + id);
  }

  // ── By slug ───────────────────────────────────────────────────────────────

  @Override
  public Optional<ProductResponse> getProductBySlug(String tenantId, String slug) {
    return get(PREFIX_SLUG + tenantId + ":" + slug);
  }

  @Override
  public void putProductBySlug(String tenantId, String slug, ProductResponse response) {
    put(PREFIX_SLUG + tenantId + ":" + slug, response);
  }

  @Override
  public void evictProductBySlug(String tenantId, String slug) {
    redisTemplate.delete(PREFIX_SLUG + tenantId + ":" + slug);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private Optional<ProductResponse> get(String key) {
    try {
      String json = redisTemplate.opsForValue().get(key);
      if (json == null) return Optional.empty();
      return Optional.of(objectMapper.readValue(json, ProductResponse.class));
    } catch (Exception e) {
      log.warn("Cache read failure for key={}: {}", key, e.getMessage());
      return Optional.empty();
    }
  }

  private void put(String key, ProductResponse response) {
    try {
      String json = objectMapper.writeValueAsString(response);
      redisTemplate.opsForValue().set(key, json, TTL);
    } catch (JsonProcessingException e) {
      log.warn("Cache write failure for key={}: {}", key, e.getMessage());
    }
  }
}
