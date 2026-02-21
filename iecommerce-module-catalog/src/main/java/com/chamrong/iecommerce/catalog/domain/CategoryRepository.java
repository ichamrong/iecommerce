package com.chamrong.iecommerce.catalog.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByTenantIdAndSlug(String tenantId, String slug);

  /** Direct children of a parent category. */
  List<Category> findByTenantIdAndParentIdOrderBySortOrderAsc(String tenantId, Long parentId);

  /** Root categories (depth=0). */
  List<Category> findByTenantIdAndDepthOrderBySortOrderAsc(String tenantId, int depth);

  /**
   * Entire subtree starting from a node — uses materialized path prefix. E.g., pathPrefix = "/1/4/"
   * returns all descendants of category 4.
   */
  @Query(
      """
      SELECT c FROM Category c
      WHERE c.tenantId = :tenantId
        AND c.materializedPath LIKE :pathPrefix
      ORDER BY c.depth ASC, c.sortOrder ASC
      """)
  List<Category> findSubtree(
      @Param("tenantId") String tenantId, @Param("pathPrefix") String pathPrefix);

  /** Check slug uniqueness per tenant. */
  boolean existsByTenantIdAndSlug(String tenantId, String slug);

  boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId);
}
