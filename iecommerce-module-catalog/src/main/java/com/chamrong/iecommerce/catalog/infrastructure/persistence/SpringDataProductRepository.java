package com.chamrong.iecommerce.catalog.infrastructure.persistence;

import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA backing store for {@link Product}. */
@Repository
interface SpringDataProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findByTenantIdAndSlug(String tenantId, String slug);

  long countByTenantId(String tenantId);

  boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId);

  // ── First page (no cursor): filter only ──────────────────────────────────

  /** First page — no status/category filter. Backed by {@code idx_products_cursor}. */
  @Query(
      "SELECT p FROM Product p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC, p.id DESC")
  List<Product> findFirstPage(@Param("tenantId") String tenantId, Pageable pageable);

  /** First page filtered by status. */
  @Query(
      "SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.status = :status ORDER BY"
          + " p.createdAt DESC, p.id DESC")
  List<Product> findFirstPageByStatus(
      @Param("tenantId") String tenantId, @Param("status") ProductStatus status, Pageable pageable);

  /** First page filtered by category. */
  @Query(
      "SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.categoryId = :categoryId ORDER BY"
          + " p.createdAt DESC, p.id DESC")
  List<Product> findFirstPageByCategory(
      @Param("tenantId") String tenantId, @Param("categoryId") Long categoryId, Pageable pageable);

  /** First page filtered by status AND category. */
  @Query(
      "SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.status = :status AND p.categoryId"
          + " = :categoryId ORDER BY p.createdAt DESC, p.id DESC")
  List<Product> findFirstPageByStatusAndCategory(
      @Param("tenantId") String tenantId,
      @Param("status") ProductStatus status,
      @Param("categoryId") Long categoryId,
      Pageable pageable);

  // ── Subsequent pages: cursor + filters ───────────────────────────────────

  /** Next page after cursor — no filter. */
  @Query(
      """
      SELECT p FROM Product p
      WHERE p.tenantId = :tenantId
        AND (p.createdAt < :afterCreatedAt
             OR (p.createdAt = :afterCreatedAt AND p.id < :afterId))
      ORDER BY p.createdAt DESC, p.id DESC
      """)
  List<Product> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  /** Next page after cursor — status filter. */
  @Query(
      """
      SELECT p FROM Product p
      WHERE p.tenantId = :tenantId
        AND p.status = :status
        AND (p.createdAt < :afterCreatedAt
             OR (p.createdAt = :afterCreatedAt AND p.id < :afterId))
      ORDER BY p.createdAt DESC, p.id DESC
      """)
  List<Product> findNextPageByStatus(
      @Param("tenantId") String tenantId,
      @Param("status") ProductStatus status,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  /** Next page after cursor — category filter. */
  @Query(
      """
      SELECT p FROM Product p
      WHERE p.tenantId = :tenantId
        AND p.categoryId = :categoryId
        AND (p.createdAt < :afterCreatedAt
             OR (p.createdAt = :afterCreatedAt AND p.id < :afterId))
      ORDER BY p.createdAt DESC, p.id DESC
      """)
  List<Product> findNextPageByCategory(
      @Param("tenantId") String tenantId,
      @Param("categoryId") Long categoryId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  /** Next page after cursor — status + category filter. */
  @Query(
      """
      SELECT p FROM Product p
      WHERE p.tenantId = :tenantId
        AND p.status = :status
        AND p.categoryId = :categoryId
        AND (p.createdAt < :afterCreatedAt
             OR (p.createdAt = :afterCreatedAt AND p.id < :afterId))
      ORDER BY p.createdAt DESC, p.id DESC
      """)
  List<Product> findNextPageByStatusAndCategory(
      @Param("tenantId") String tenantId,
      @Param("status") ProductStatus status,
      @Param("categoryId") Long categoryId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);

  // ── Full-text search via GIN index (native SQL) ───────────────────────────

  /**
   * FTS first-page using the GIN index on {@code catalog_product_translations}. Uses {@code
   * plainto_tsquery} so plain keywords (no operators) work.
   */
  @Query(
      value =
          """
          SELECT DISTINCT p.* FROM catalog_products p
          JOIN catalog_product_translations t ON t.product_id = p.id
          WHERE p.tenant_id = :tenantId
            AND p.deleted = FALSE
            AND to_tsvector('simple', t.name || ' ' || COALESCE(t.description, ''))
                @@ plainto_tsquery('simple', :keyword)
          ORDER BY p.created_at DESC, p.id DESC
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Product> searchFirstPage(
      @Param("tenantId") String tenantId,
      @Param("keyword") String keyword,
      @Param("limit") int limit);

  /** FTS subsequent pages — keyset after (created_at, id). */
  @Query(
      value =
          """
          SELECT DISTINCT p.* FROM catalog_products p
          JOIN catalog_product_translations t ON t.product_id = p.id
          WHERE p.tenant_id = :tenantId
            AND p.deleted = FALSE
            AND to_tsvector('simple', t.name || ' ' || COALESCE(t.description, ''))
                @@ plainto_tsquery('simple', :keyword)
            AND (p.created_at < :afterCreatedAt
                 OR (p.created_at = :afterCreatedAt AND p.id < :afterId))
          ORDER BY p.created_at DESC, p.id DESC
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Product> searchNextPage(
      @Param("tenantId") String tenantId,
      @Param("keyword") String keyword,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      @Param("limit") int limit);
}
