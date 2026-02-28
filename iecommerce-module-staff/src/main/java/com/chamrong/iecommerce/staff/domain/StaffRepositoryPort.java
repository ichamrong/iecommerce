package com.chamrong.iecommerce.staff.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port through which the Application layer interacts with Staff persistence.
 *
 * <p>Implementors live in the infrastructure layer and are injected via Spring DI.
 */
public interface StaffRepositoryPort {
  Optional<StaffProfile> findByUserId(String userId);

  Optional<StaffProfile> findById(Long id);

  boolean existsByUserId(String userId);

  StaffProfile save(StaffProfile profile);

  /**
   * Keyset (cursor) paginated fetch sorted by {@code created_at DESC, id DESC}.
   *
   * <p>Pass {@code null} for both {@code afterCreatedAt} and {@code afterId} to get the first page.
   * Pass the {@code createdAt} and {@code id} of the last element of the previous page to advance.
   *
   * @param afterCreatedAt exclusive upper bound on created_at; null → first page
   * @param afterId exclusive upper bound on id when created_at is equal; null → first page
   * @param limit max rows to return (callers request limit+1 to detect hasNext)
   */
  List<StaffProfile> findNextPage(Instant afterCreatedAt, Long afterId, int limit);
}
