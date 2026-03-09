package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.Role;
import java.util.List;
import java.util.Optional;

/** Port for role persistence. Implemented by infrastructure adapters. */
public interface RoleRepositoryPort {

  Optional<Role> findByName(String name);

  List<Role> findAll();

  Role save(Role role);
}
