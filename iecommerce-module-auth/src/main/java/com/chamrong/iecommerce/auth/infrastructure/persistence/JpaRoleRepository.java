package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepository {

  @Override
  @Cacheable(value = "roles", key = "#name")
  Optional<Role> findByName(String name);

  @Override
  @CacheEvict(value = "roles", key = "#role.name")
  Role save(Role role);
}
