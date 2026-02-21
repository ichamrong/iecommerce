package com.chamrong.iecommerce.setting.infrastructure;

import com.chamrong.iecommerce.setting.domain.TenantSetting;
import com.chamrong.iecommerce.setting.domain.TenantSettingRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link TenantSettingRepository} port. */
@Repository
public interface JpaTenantSettingRepository
    extends JpaRepository<TenantSetting, Long>, TenantSettingRepository {
  @Override
  Optional<TenantSetting> findByKey(String key);
}
