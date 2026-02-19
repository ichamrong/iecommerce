package com.chamrong.iecommerce.setting.domain;

import java.util.Optional;

public interface TenantSettingRepository {
  TenantSetting save(TenantSetting setting);

  Optional<TenantSetting> findByKey(String key);
}
