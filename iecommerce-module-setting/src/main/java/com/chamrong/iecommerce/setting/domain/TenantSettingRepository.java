package com.chamrong.iecommerce.setting.domain;

import java.util.List;
import java.util.Optional;

/** Port for tenant-specific setting persistence. */
public interface TenantSettingRepository {
  TenantSetting save(TenantSetting setting);

  Optional<TenantSetting> findByTenantIdAndKey(String tenantId, String key);

  /** Legacy fallback — prefer {@link #findByTenantIdAndKey(String, String)}. */
  Optional<TenantSetting> findByKey(String key);

  List<TenantSetting> findByTenantId(String tenantId);

  List<TenantSetting> findByTenantIdAndCategory(String tenantId, SettingCategory category);

  void deleteByTenantIdAndKey(String tenantId, String key);
}
