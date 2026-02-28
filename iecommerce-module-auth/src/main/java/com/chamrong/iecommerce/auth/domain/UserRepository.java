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

  /**
   * @deprecated Use {@link #findByUsernameAndTenantId(String, String)} instead.
   */
  @Deprecated(since = "multi-tenant", forRemoval = true)
  Optional<User> findByUsername(String username);

  /**
   * @deprecated Use {@link #findByEmailAndTenantId(String, String)} instead.
   */
  @Deprecated(since = "multi-tenant", forRemoval = true)
  Optional<User> findByEmail(String email);

  User save(User user);

  /**
   * @deprecated Use {@link #findByTenantId(String)} instead.
   */
  @Deprecated(since = "multi-tenant", forRemoval = true)
  List<User> findAll();
}
