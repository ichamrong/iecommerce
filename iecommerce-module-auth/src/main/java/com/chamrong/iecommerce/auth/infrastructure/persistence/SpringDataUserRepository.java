package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for User. Used by {@link JpaUserRepositoryAdapter}. */
@Repository
public interface SpringDataUserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsernameAndTenantId(String username, String tenantId);

  Optional<User> findByKeycloakId(String keycloakId);

  Optional<User> findByEmailAndTenantId(String email, String tenantId);

  List<User> findByTenantId(String tenantId);

  @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId ORDER BY u.createdAt DESC, u.id DESC")
  List<User> findFirstPageByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

  @Query(
      "SELECT u FROM User u WHERE u.tenantId = :tenantId AND (u.createdAt < :ca OR (u.createdAt ="
          + " :ca AND u.id < :id)) ORDER BY u.createdAt DESC, u.id DESC")
  List<User> findNextPageByTenantId(
      @Param("tenantId") String tenantId,
      @Param("ca") Instant ca,
      @Param("id") Long id,
      Pageable pageable);

  @Deprecated(since = "multi-tenant")
  Optional<User> findByUsername(String username);

  @Deprecated(since = "multi-tenant")
  Optional<User> findByEmail(String email);
}
