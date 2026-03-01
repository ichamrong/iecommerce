package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Implements {@link UserRepositoryPort} using {@link SpringDataUserRepository}. */
@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

  private static final Sort KEYSET_SORT = Sort.by(Sort.Direction.DESC, "createdAt", "id");

  private final SpringDataUserRepository repository;

  @Override
  public Optional<User> findById(Long id) {
    return repository.findById(id);
  }

  @Override
  public Optional<User> findByUsernameAndTenantId(String username, String tenantId) {
    return repository.findByUsernameAndTenantId(username, tenantId);
  }

  @Override
  public Optional<User> findByKeycloakId(String keycloakId) {
    return repository.findByKeycloakId(keycloakId);
  }

  @Override
  public Optional<User> findByEmailAndTenantId(String email, String tenantId) {
    return repository.findByEmailAndTenantId(email, tenantId);
  }

  @Override
  public List<User> findByTenantId(String tenantId) {
    return repository.findByTenantId(tenantId);
  }

  @Override
  @Deprecated(since = "multi-tenant", forRemoval = true)
  public Optional<User> findByUsername(String username) {
    return repository.findByUsername(username);
  }

  @Override
  public List<User> findPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    PageRequest page = PageRequest.of(0, limitPlusOne, KEYSET_SORT);
    if (cursorCreatedAt == null || cursorId == null) {
      return repository.findFirstPageByTenantId(tenantId, page);
    }
    return repository.findNextPageByTenantId(tenantId, cursorCreatedAt, cursorId, page);
  }

  @Override
  public User save(User user) {
    return repository.save(user);
  }
}
