package com.chamrong.iecommerce.auth.domain;

import java.util.Optional;

public interface TenantRepository {
  Optional<Tenant> findByCode(String code);

  boolean existsByCode(String code);

  Tenant save(Tenant tenant);
}
