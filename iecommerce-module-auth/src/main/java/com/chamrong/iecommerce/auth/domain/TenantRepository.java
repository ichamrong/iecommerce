package com.chamrong.iecommerce.auth.domain;

import java.util.Optional;

public interface TenantRepository {
  Optional<Tenant> findByCode(String code);

  Tenant save(Tenant tenant);
}
