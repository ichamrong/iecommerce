package com.chamrong.iecommerce.setting.infrastructure;

import com.chamrong.iecommerce.setting.domain.GlobalSetting;
import com.chamrong.iecommerce.setting.domain.GlobalSettingRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaGlobalSettingRepository implements GlobalSettingRepository {

  private final GlobalSettingJpaInterface jpaInterface;

  public JpaGlobalSettingRepository(GlobalSettingJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public GlobalSetting save(GlobalSetting setting) {
    return jpaInterface.save(setting);
  }

  @Override
  public Optional<GlobalSetting> findByKey(String key) {
    return jpaInterface.findByKey(key);
  }

  public interface GlobalSettingJpaInterface extends JpaRepository<GlobalSetting, Long> {
    Optional<GlobalSetting> findByKey(String key);
  }
}
