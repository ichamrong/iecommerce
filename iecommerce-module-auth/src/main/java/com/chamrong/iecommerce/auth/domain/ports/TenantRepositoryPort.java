package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.Tenant;
import java.util.List;
import java.util.Optional;

/** Port for tenant persistence. Implemented by infrastructure persistence adapters. */
public interface TenantRepositoryPort {

  Optional<Tenant> findByCode(String code);

  boolean existsByCode(String code);

  List<Tenant> findAll();

  Tenant save(Tenant tenant);
}
