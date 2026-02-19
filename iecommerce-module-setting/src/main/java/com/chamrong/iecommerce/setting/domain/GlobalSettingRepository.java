package com.chamrong.iecommerce.setting.domain;

import java.util.Optional;

public interface GlobalSettingRepository {
  GlobalSetting save(GlobalSetting setting);

  Optional<GlobalSetting> findByKey(String key);
}
