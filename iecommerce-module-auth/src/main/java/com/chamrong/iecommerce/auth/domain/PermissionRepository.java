package com.chamrong.iecommerce.auth.domain;

import java.util.Optional;

public interface PermissionRepository {
  Optional<Permission> findByName(String name);

  Permission save(Permission permission);
}
