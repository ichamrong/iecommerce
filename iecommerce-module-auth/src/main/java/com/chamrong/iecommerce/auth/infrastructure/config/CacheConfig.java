package com.chamrong.iecommerce.auth.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the auth module.
 *
 * <p>Uses per-cache TTL and size tuning rather than a single global Caffeine spec, because
 * different data sets have very different staleness tolerances:
 *
 * <table>
 *   <tr><th>Cache</th><th>TTL</th><th>Max</th><th>Rationale</th></tr>
 *   <tr><td>roles</td><td>60 min</td><td>1000</td><td>Rarely changes; admin action invalidates</td></tr>
 *   <tr><td>permissions</td><td>60 min</td><td>5000</td><td>Rarely changes</td></tr>
 *   <tr><td>tenants</td><td>30 min</td><td>1000</td><td>Tenant prefs can change via admin</td></tr>
 *   <tr><td>user_profiles</td><td>5 min</td><td>10000</td><td>Frequently read; short TTL for freshness</td></tr>
 *   <tr><td>social_providers</td><td>60 min</td><td>1</td><td>Static Keycloak realm config</td></tr>
 * </table>
 */
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean("authCacheManager")
  public CacheManager cacheManager() {
    final SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(
        java.util.List.of(
            build("roles", 60, TimeUnit.MINUTES, 1_000),
            build("permissions", 60, TimeUnit.MINUTES, 5_000),
            build("tenants", 30, TimeUnit.MINUTES, 1_000),
            build("user_profiles", 5, TimeUnit.MINUTES, 10_000),
            build("social_providers", 60, TimeUnit.MINUTES, 1),
            build("identity_ids", 10, TimeUnit.MINUTES, 5_000)));
    return manager;
  }

  private static CaffeineCache build(
      final String name, final long duration, final TimeUnit unit, final long maxSize) {
    return new CaffeineCache(
        name,
        Caffeine.newBuilder()
            .expireAfterWrite(duration, unit)
            .maximumSize(maxSize)
            .recordStats() // enables Micrometer metrics via CaffeineCacheMetrics
            .build());
  }
}
