package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link UserRepository} port. */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

  @Override
  Optional<User> findByUsernameAndTenantId(String username, String tenantId);

  @Override
  Optional<User> findByKeycloakId(String keycloakId);

  @Override
  Optional<User> findByEmailAndTenantId(String email, String tenantId);

  @Override
  List<User> findByTenantId(String tenantId);

  @Override
  Page<User> findByTenantId(String tenantId, Pageable pageable);

  @Override
  @Deprecated(since = "multi-tenant")
  Optional<User> findByUsername(String username);

  @Override
  @Deprecated(since = "multi-tenant")
  Optional<User> findByEmail(String email);

  @Override
  @Deprecated(since = "multi-tenant")
  default @NonNull List<User> findAll() {
    throw new UnsupportedOperationException(
        "findAll() is forbidden in multi-tenant context. Use findByTenantId(tenantId).");
  }
}
