package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.ports.PermissionRepositoryPort;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository implementing {@link PermissionRepositoryPort}. */
@Repository
public interface JpaPermissionRepository
    extends JpaRepository<Permission, Long>, PermissionRepositoryPort {

  @Override
  @Cacheable(value = "permissions", key = "#name", cacheManager = "authCacheManager")
  Optional<Permission> findByName(String name);

  @Override
  @CacheEvict(value = "permissions", key = "#permission.name", cacheManager = "authCacheManager")
  Permission save(Permission permission);
}
