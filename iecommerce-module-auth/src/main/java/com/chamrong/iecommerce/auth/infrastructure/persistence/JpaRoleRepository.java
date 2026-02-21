package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepository {

  @Override
  Optional<Role> findByName(String name);
}
