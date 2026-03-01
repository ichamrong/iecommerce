package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.Permission;
import java.util.Optional;

/** Port for permission persistence. Implemented by infrastructure adapters. */
public interface PermissionRepositoryPort {

  Optional<Permission> findByName(String name);

  Permission save(Permission permission);
}
