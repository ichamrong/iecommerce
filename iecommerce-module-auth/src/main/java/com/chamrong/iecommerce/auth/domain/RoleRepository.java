package com.chamrong.iecommerce.auth.domain;

import java.util.Optional;

public interface RoleRepository {
  Optional<Role> findByName(String name);

  Role save(Role role);
}
