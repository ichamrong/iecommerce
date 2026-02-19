package com.chamrong.iecommerce.setting.application;

import com.chamrong.iecommerce.setting.domain.GlobalSetting;
import com.chamrong.iecommerce.setting.domain.GlobalSettingRepository;
import com.chamrong.iecommerce.setting.domain.TenantSetting;
import com.chamrong.iecommerce.setting.domain.TenantSettingRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService {

  private final GlobalSettingRepository globalSettingRepository;
  private final TenantSettingRepository tenantSettingRepository;

  public SettingService(
      GlobalSettingRepository globalSettingRepository,
      TenantSettingRepository tenantSettingRepository) {
    this.globalSettingRepository = globalSettingRepository;
    this.tenantSettingRepository = tenantSettingRepository;
  }

  public Optional<String> getGlobalValue(String key) {
    return globalSettingRepository.findByKey(key).map(GlobalSetting::getValue);
  }

  public Optional<String> getTenantValue(String key) {
    return tenantSettingRepository.findByKey(key).map(TenantSetting::getValue);
  }

  @Transactional
  public void setGlobalValue(String key, String value) {
    GlobalSetting setting = globalSettingRepository.findByKey(key).orElse(new GlobalSetting());
    setting.setKey(key);
    setting.setValue(value);
    globalSettingRepository.save(setting);
  }

  @Transactional
  public void setTenantValue(String key, String value) {
    TenantSetting setting = tenantSettingRepository.findByKey(key).orElse(new TenantSetting());
    setting.setKey(key);
    setting.setValue(value);
    tenantSettingRepository.save(setting);
  }
}
