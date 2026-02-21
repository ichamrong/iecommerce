package com.chamrong.iecommerce.auth.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
  Optional<User> findById(Long id);

  Optional<User> findByUsernameAndTenantId(String username, String tenantId);

  Optional<User> findByKeycloakId(String keycloakId);

  Optional<User> findByEmailAndTenantId(String email, String tenantId);

  List<User> findByTenantId(String tenantId);

  Page<User> findByTenantId(String tenantId, Pageable pageable);

  @Deprecated(since = "multi-tenant")
  Optional<User> findByUsername(String username);

  @Deprecated(since = "multi-tenant")
  Optional<User> findByEmail(String email);

  User save(User user);

  @Deprecated(since = "multi-tenant")
  List<User> findAll();
}
