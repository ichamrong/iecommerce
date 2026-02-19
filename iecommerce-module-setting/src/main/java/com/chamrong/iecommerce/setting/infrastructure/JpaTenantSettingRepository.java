package com.chamrong.iecommerce.setting.infrastructure;

import com.chamrong.iecommerce.setting.domain.TenantSetting;
import com.chamrong.iecommerce.setting.domain.TenantSettingRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTenantSettingRepository implements TenantSettingRepository {

  private final TenantSettingJpaInterface jpaInterface;

  public JpaTenantSettingRepository(TenantSettingJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public TenantSetting save(TenantSetting setting) {
    return jpaInterface.save(setting);
  }

  @Override
  public Optional<TenantSetting> findByKey(String key) {
    return jpaInterface.findByKey(key);
  }

  public interface TenantSettingJpaInterface extends JpaRepository<TenantSetting, Long> {
    Optional<TenantSetting> findByKey(String key);
  }
}
