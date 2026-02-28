package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import java.util.Optional;

/**
 * Port for catalog read-side caching.
 *
 * <p>Implementations live in {@code infrastructure/cache}. A no-op implementation is also provided
 * for environments without Redis.
 */
public interface CatalogCachePort {

  Optional<ProductResponse> getProduct(Long id);

  void putProduct(Long id, ProductResponse response);

  void evictProduct(Long id);

  Optional<ProductResponse> getProductBySlug(String tenantId, String slug);

  void putProductBySlug(String tenantId, String slug, ProductResponse response);

  void evictProductBySlug(String tenantId, String slug);
}
