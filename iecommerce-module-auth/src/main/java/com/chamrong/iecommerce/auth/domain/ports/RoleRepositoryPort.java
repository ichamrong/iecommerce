package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.Role;
import java.util.Optional;

/** Port for role persistence. Implemented by infrastructure adapters. */
public interface RoleRepositoryPort {

  Optional<Role> findByName(String name);

  Role save(Role role);
}
