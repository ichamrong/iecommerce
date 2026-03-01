package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for user persistence. Implemented by infrastructure adapters.
 *
 * <p>List endpoint uses keyset pagination via {@link #findPage(String, Instant, Long, int)}. No
 * Spring Page/Pageable in domain.
 */
public interface UserRepositoryPort {

  Optional<User> findById(Long id);

  Optional<User> findByUsernameAndTenantId(String username, String tenantId);

  Optional<User> findByKeycloakId(String keycloakId);

  Optional<User> findByEmailAndTenantId(String email, String tenantId);

  List<User> findByTenantId(String tenantId);

  /**
   * @deprecated Use {@link #findByUsernameAndTenantId(String, String)} instead.
   */
  @Deprecated(since = "multi-tenant", forRemoval = true)
  Optional<User> findByUsername(String username);

  /**
   * Keyset page: tenant_id, then (created_at &lt; cursorCreatedAt OR (created_at = cursorCreatedAt
   * AND id &lt; cursorId)), ORDER BY created_at DESC, id DESC, LIMIT limitPlusOne.
   *
   * @param cursorCreatedAt null for first page
   * @param cursorId null for first page
   */
  List<User> findPage(String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  User save(User user);
}
