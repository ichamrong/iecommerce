package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.PermissionRepository;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link PermissionRepository} port. */
@Repository
public interface JpaPermissionRepository
    extends JpaRepository<Permission, Long>, PermissionRepository {

  @Override
  @Cacheable(value = "permissions", key = "#name")
  Optional<Permission> findByName(String name);

  @Override
  @CacheEvict(value = "permissions", key = "#permission.name")
  Permission save(Permission permission);
}
