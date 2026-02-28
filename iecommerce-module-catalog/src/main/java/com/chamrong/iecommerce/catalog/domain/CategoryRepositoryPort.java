package com.chamrong.iecommerce.catalog.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port through which the Application layer interacts with Category persistence.
 *
 * <p>Implementors live in {@code infrastructure/persistence}.
 */
public interface CategoryRepositoryPort {

  Optional<Category> findById(Long id);

  List<Category> findByTenantId(String tenantId);

  Category save(Category category);

  void delete(Category category);

  List<Category> findDescendants(String materializedPathPrefix, String tenantId);

  /**
   * Keyset paginated category listing sorted by (created_at DESC, id DESC).
   *
   * @param afterCreatedAt cursor; null → first page
   * @param afterId cursor tie-break; null → first page
   * @param limit max rows
   */
  List<Category> findCursorPage(String tenantId, Instant afterCreatedAt, Long afterId, int limit);
}
