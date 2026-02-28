package com.chamrong.iecommerce.catalog.infrastructure.cache;

import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op cache fallback used when Redis is not configured.
 *
 * <p>Activated whenever no other {@link CatalogCachePort} bean is registered (i.e., when {@link
 * RedisCatalogCacheAdapter} does not register because {@code RedisTemplate} is absent from the
 * context).
 *
 * <p>Always returns {@link Optional#empty()} on read and silently discards writes.
 */
@Component
@ConditionalOnMissingBean(CatalogCachePort.class)
@SuppressWarnings("java:S1186") // empty methods are intentional — no-op by design
public class NoOpCatalogCacheAdapter implements CatalogCachePort {

  @Override
  public Optional<ProductResponse> getProduct(Long id) {
    return Optional.empty();
  }

  @Override
  public void putProduct(Long id, ProductResponse response) {
    /* no-op */
  }

  @Override
  public void evictProduct(Long id) {
    /* no-op */
  }

  @Override
  public Optional<ProductResponse> getProductBySlug(String tenantId, String slug) {
    return Optional.empty();
  }

  @Override
  public void putProductBySlug(String tenantId, String slug, ProductResponse response) {
    /* no-op */
  }

  @Override
  public void evictProductBySlug(String tenantId, String slug) {
    /* no-op */
  }
}
