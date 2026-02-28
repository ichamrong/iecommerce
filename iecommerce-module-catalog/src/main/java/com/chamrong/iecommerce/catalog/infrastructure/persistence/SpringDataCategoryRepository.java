package com.chamrong.iecommerce.catalog.infrastructure.persistence;

import com.chamrong.iecommerce.catalog.domain.Category;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA backing store for {@link Category}. */
@Repository
interface SpringDataCategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByTenantId(String tenantId);

  @Query(
      """
      SELECT c FROM Category c
      WHERE c.tenantId = :tenantId
        AND c.materializedPath LIKE :pathPrefix
      ORDER BY c.depth ASC, c.sortOrder ASC
      """)
  List<Category> findDescendants(
      @Param("tenantId") String tenantId, @Param("pathPrefix") String pathPrefix);

  @Query(
      "SELECT c FROM Category c WHERE c.tenantId = :tenantId ORDER BY c.createdAt DESC, c.id DESC")
  List<Category> findFirstPage(@Param("tenantId") String tenantId, Pageable pageable);

  @Query(
      """
      SELECT c FROM Category c
      WHERE c.tenantId = :tenantId
        AND (c.createdAt < :afterCreatedAt
             OR (c.createdAt = :afterCreatedAt AND c.id < :afterId))
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Category> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("afterCreatedAt") Instant afterCreatedAt,
      @Param("afterId") Long afterId,
      Pageable pageable);
}
