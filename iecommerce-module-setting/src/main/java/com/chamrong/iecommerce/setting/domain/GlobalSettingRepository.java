package com.chamrong.iecommerce.setting.domain;

import java.util.List;
import java.util.Optional;

/** Port for global setting persistence. */
public interface GlobalSettingRepository {
  GlobalSetting save(GlobalSetting setting);

  Optional<GlobalSetting> findByKey(String key);

  List<GlobalSetting> findAll();

  List<GlobalSetting> findByCategory(SettingCategory category);

  void deleteByKey(String key);
}
