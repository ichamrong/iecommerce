package com.chamrong.iecommerce.catalog;

import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.infrastructure.cache.NoOpCatalogCacheAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level wiring for catalog cache.
 *
 * <p>Provides a simple no-op {@link CatalogCachePort} so that the application can start even when
 * Redis is not configured. The Redis-backed adapter will still be used automatically in
 * environments where a {@code RedisTemplate} bean is present.
 */
@Configuration
public class CatalogCacheConfiguration {

  @Bean
  public CatalogCachePort catalogCachePort() {
    return new NoOpCatalogCacheAdapter();
  }
}
