package com.chamrong.iecommerce.asset.application.service;

import com.chamrong.iecommerce.setting.application.QuotaEnforcer;
import com.chamrong.iecommerce.setting.domain.SettingKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Resolves per-tenant storage quota for assets.
 *
 * <p>Current strategy:
 *
 * <ul>
 *   <li>If a numeric quota is configured under {@code quota.max_media_mb} for the tenant, that
 *       value (in megabytes) is converted to bytes.
 *   <li>If no quota is configured (SettingService returns {@link Integer#MAX_VALUE}), the tenant
 *       has effectively unlimited storage.
 *   <li>If the settings module is unavailable for any reason, falls back to a conservative default
 *       of 5GB.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetQuotaService {

  /** 5GB default, only used when settings are unavailable. */
  private static final long FALLBACK_QUOTA_BYTES = 5L * 1024 * 1024 * 1024;

  private static final long BYTES_PER_MB = 1024L * 1024L;

  private final QuotaEnforcer quotaEnforcer;

  /**
   * Returns the maximum allowed storage in bytes for the given tenant.
   *
   * @param tenantId tenant identifier
   * @return quota in bytes, or {@link Long#MAX_VALUE} when unlimited
   */
  public long getQuotaForTenant(String tenantId) {
    try {
      int limitMb = quotaEnforcer.getLimit(tenantId, SettingKeys.QUOTA_MAX_MEDIA_MB);
      if (limitMb == Integer.MAX_VALUE) {
        return Long.MAX_VALUE;
      }
      if (limitMb <= 0) {
        // Non-positive configured limit is treated as \"no storage\".
        return 0L;
      }
      return Math.min(Long.MAX_VALUE, limitMb * BYTES_PER_MB);
    } catch (Exception ex) {
      log.warn(
          "Falling back to default asset quota for tenant={} due to error resolving settings: {}",
          tenantId,
          ex.getMessage());
      return FALLBACK_QUOTA_BYTES;
    }
  }
}
