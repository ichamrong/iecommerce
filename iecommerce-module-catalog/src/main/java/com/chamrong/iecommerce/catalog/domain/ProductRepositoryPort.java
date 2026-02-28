package com.chamrong.iecommerce.catalog.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port through which the Application layer interacts with Product persistence.
 *
 * <p>Implementors live in {@code infrastructure/persistence}. The Application layer never imports a
 * concrete Spring Data repository.
 */
public interface ProductRepositoryPort {

  Optional<Product> findById(Long id);

  Optional<Product> findByTenantIdAndSlug(String tenantId, String slug);

  boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId);

  long countByTenantId(String tenantId);

  Product save(Product product);

  void delete(Product product);

  /**
   * Keyset paginated query with optional filters and keyword search.
   *
   * <p>Pass {@code null} for {@code afterCreatedAt} and {@code afterId} to get the first page.
   * Filters are AND-combined; null values are ignored.
   *
   * @param tenantId required tenant scope
   * @param status optional status filter
   * @param categoryId optional category filter
   * @param keyword optional full-text keyword (searches name + description via GIN index)
   * @param afterCreatedAt cursor upper bound on created_at; null → first page
   * @param afterId cursor tie-break on id; null → first page
   * @param limit max rows (callers request limit+1 to detect hasNext)
   */
  List<Product> findCursorPage(
      String tenantId,
      ProductStatus status,
      Long categoryId,
      String keyword,
      Instant afterCreatedAt,
      Long afterId,
      int limit);
}
