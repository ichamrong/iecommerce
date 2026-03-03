package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepositoryPort {

  @Override
  @Cacheable(value = "roles", key = "#name", cacheManager = "authCacheManager")
  Optional<Role> findByName(String name);

  @Override
  @CacheEvict(value = "roles", key = "#role.name", cacheManager = "authCacheManager")
  Role save(Role role);
}
