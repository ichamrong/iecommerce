package com.chamrong.iecommerce.setting.infrastructure;

import com.chamrong.iecommerce.setting.domain.GlobalSetting;
import com.chamrong.iecommerce.setting.domain.GlobalSettingRepository;
import com.chamrong.iecommerce.setting.domain.SettingCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link GlobalSettingRepository} port. */
@Repository
public interface JpaGlobalSettingRepository
    extends JpaRepository<GlobalSetting, Long>, GlobalSettingRepository {

  @Override
  Optional<GlobalSetting> findByKey(String key);

  @Override
  List<GlobalSetting> findByCategory(SettingCategory category);

  @Override
  void deleteByKey(String key);
}
