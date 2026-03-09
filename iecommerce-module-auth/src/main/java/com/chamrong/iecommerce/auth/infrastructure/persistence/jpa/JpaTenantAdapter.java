package com.chamrong.iecommerce.auth.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Implements TenantRepositoryPort using TenantEntity and TenantPersistenceMapper. */
@Component
public class JpaTenantAdapter implements TenantRepositoryPort {

  private final SpringDataTenantRepository repository;
  private final TenantPersistenceMapper mapper;

  public JpaTenantAdapter(SpringDataTenantRepository repository, TenantPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Cacheable(value = "tenants", key = "#code", cacheManager = "authCacheManager")
  public java.util.Optional<Tenant> findByCode(String code) {
    return repository.findByCode(code).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.List<Tenant> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  @Cacheable(value = "tenants", key = "'exists:' + #code", cacheManager = "authCacheManager")
  public boolean existsByCode(String code) {
    return repository.existsByCode(code);
  }

  @Override
  @CacheEvict(value = "tenants", key = "#tenant.code", cacheManager = "authCacheManager")
  public Tenant save(Tenant tenant) {
    var entity = mapper.toEntity(tenant);
    var saved = repository.save(entity);
    return mapper.toDomain(saved);
  }

  @CacheEvict(
      value = "tenants",
      key = "'exists:' + #tenant.code",
      cacheManager = "authCacheManager")
  public void evictExistsCache(Tenant tenant) {}
}
