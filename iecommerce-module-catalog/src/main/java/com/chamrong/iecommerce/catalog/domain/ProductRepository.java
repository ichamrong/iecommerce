package com.chamrong.iecommerce.catalog.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /** Storefront slug lookup — unique per tenant. */
  Optional<Product> findByTenantIdAndSlug(String tenantId, String slug);

  /** Check slug availability before creating/updating. */
  boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId);

  /** Admin product list filtered by tenant and status. */
  List<Product> findByTenantIdAndStatus(String tenantId, ProductStatus status);

  /** All products under a specific category. */
  List<Product> findByTenantIdAndCategoryId(String tenantId, Long categoryId);

  /**
   * All products under a category subtree — uses materialized path prefix match. Joins to
   * catalog_categories on category_id, then filters by path.
   */
  @Query(
      """
      SELECT p FROM Product p
      JOIN Category c ON c.id = p.categoryId
      WHERE p.tenantId = :tenantId
        AND c.materializedPath LIKE :pathPrefix
      """)
  List<Product> findByTenantIdAndCategorySubtree(
      @Param("tenantId") String tenantId, @Param("pathPrefix") String pathPrefix);
}
