package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link TenantRepository} port. */
@Repository
public interface JpaTenantRepository extends JpaRepository<Tenant, Long>, TenantRepository {

  @Override
  @Cacheable(value = "tenants", key = "#code")
  Optional<Tenant> findByCode(String code);

  @Override
  @Cacheable(value = "tenants", key = "'exists:' + #code")
  boolean existsByCode(String code);

  @Override
  @CacheEvict(value = "tenants", key = "#tenant.code")
  Tenant save(Tenant tenant);

  @CacheEvict(value = "tenants", key = "'exists:' + #tenant.code")
  default void evictExistsCache(Tenant tenant) {}
}
