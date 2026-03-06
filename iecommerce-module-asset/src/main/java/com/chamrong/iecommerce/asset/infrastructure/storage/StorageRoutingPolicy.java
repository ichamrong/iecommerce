package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageProvider;
import com.chamrong.iecommerce.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Pluggable strategy for resolving which storage provider to use for a given tenant.
 *
 * <p>Current behaviour:
 *
 * <ul>
 *   <li>If a current tenant is present in {@link TenantContext}, returns the provider configured in
 *       {@link StorageRoutingConfiguration}.
 *   <li>If no tenant context is available, falls back to the global default provider key.
 * </ul>
 *
 * <p>This indirection allows future per-tenant routing (e.g. enterprise tenants stored in a
 * dedicated region or provider) without changing the core {@link
 * com.chamrong.iecommerce.asset.domain.StorageService} callers.
 */
@Component
@RequiredArgsConstructor
public class StorageRoutingPolicy {

  private final StorageRoutingConfiguration config;

  /**
   * Resolve the storage provider key for the given tenant.
   *
   * @param tenantId current tenant or {@code null}
   * @return a provider key understood by {@link StorageProvider} and {@link StorageRoutingService}
   */
  public String resolveProviderForTenant(String tenantId) {
    // For now, every tenant shares the same provider as configured globally.
    StorageProvider provider = config.getProvider();
    if (provider == null) {
      return StorageConstants.DEFAULT_PROVIDER;
    }
    return provider.getKey();
  }

  /** Convenience overload that reads the tenant from {@link TenantContext}. */
  public String resolveForCurrentTenant() {
    String tenantId = TenantContext.getCurrentTenant();
    return resolveProviderForTenant(tenantId);
  }
}
