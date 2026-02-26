package com.chamrong.iecommerce.setting.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Centralised quota enforcement service.
 *
 * <p>Any module that needs to check whether a tenant has reached a plan limit calls this service
 * rather than hardcoding the limit. Settings are looked up from {@link SettingService}, which
 * cascades: tenant override → global platform default → unlimited.
 *
 * <h3>Quota Keys (examples)</h3>
 *
 * <pre>
 *   quota.max_products         – Maximum catalog products per tenant
 *   quota.max_staff            – Maximum staff accounts per tenant
 *   quota.max_customers        – Maximum customer records per tenant
 *   quota.max_warehouses       – Maximum inventory warehouses per tenant
 *   quota.max_media_mb         – Total media storage in megabytes per tenant
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaEnforcer {

  private final SettingService settingService;

  /**
   * Asserts that the tenant has not exceeded the quota for the given key.
   *
   * @param tenantId the tenant to check
   * @param quotaKey the setting key that holds the numeric limit
   * @param current  the current count or usage
   * @throws QuotaExceededException if {@code current >= limit}
   */
  public void enforce(String tenantId, String quotaKey, long current) {
    int limit = settingService.getQuota(tenantId, quotaKey);
    if (current >= limit) {
      log.warn(
          "Quota exceeded for tenant={} key={} current={} limit={}",
          tenantId, quotaKey, current, limit);
      throw new QuotaExceededException(quotaKey, current, limit);
    }
  }

  /**
   * Checks whether the tenant is within quota without throwing.
   *
   * @return {@code true} if usage is below the configured limit
   */
  public boolean isWithinQuota(String tenantId, String quotaKey, long current) {
    int limit = settingService.getQuota(tenantId, quotaKey);
    return current < limit;
  }

  /**
   * Returns the configured numeric limit for the quota key for the given tenant.
   *
   * @return {@link Integer#MAX_VALUE} if no quota is configured (unlimited)
   */
  public int getLimit(String tenantId, String quotaKey) {
    return settingService.getQuota(tenantId, quotaKey);
  }
}
