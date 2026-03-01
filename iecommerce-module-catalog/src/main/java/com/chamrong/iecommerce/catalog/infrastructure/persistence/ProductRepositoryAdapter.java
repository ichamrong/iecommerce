package com.chamrong.iecommerce.catalog.infrastructure.persistence;

import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link ProductRepositoryPort}.
 *
 * <p>Delegates to {@link SpringDataProductRepository} for all queries. Cursor pagination uses
 * keyset semantics on (created_at DESC, id DESC) — O(log N) at any depth, backed by {@code
 * idx_products_cursor}.
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

  private final SpringDataProductRepository jpaRepo;

  @Override
  public Optional<Product> findById(Long id) {
    return jpaRepo.findById(id);
  }

  @Override
  public Optional<Product> findByTenantIdAndSlug(String tenantId, String slug) {
    return jpaRepo.findByTenantIdAndSlug(tenantId, slug);
  }

  @Override
  public Optional<Product> findByTenantIdAndVariantSku(String tenantId, String sku) {
    return jpaRepo.findByTenantIdAndVariantSku(tenantId, sku);
  }

  @Override
  public Optional<Product> findByTenantIdAndBarcode(String tenantId, String barcode) {
    return jpaRepo.findByTenantIdAndBarcode(tenantId, barcode);
  }

  @Override
  public boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId) {
    return jpaRepo.existsByTenantIdAndSlugAndIdNot(tenantId, slug, excludeId);
  }

  @Override
  public long countByTenantId(String tenantId) {
    return jpaRepo.countByTenantId(tenantId);
  }

  @Override
  public Product save(Product product) {
    return jpaRepo.save(product);
  }

  @Override
  public void delete(Product product) {
    jpaRepo.delete(product);
  }

  /**
   * {@inheritDoc}
   *
   * <p>If {@code keyword} is non-null, delegates to native FTS queries that use the GIN index on
   * {@code catalog_product_translations}. Otherwise, uses JPQL keyset queries.
   *
   * <p>Note: FTS queries ignore {@code status} and {@code categoryId} filters for simplicity. They
   * can be added as additional WHERE clauses if needed.
   */
  @Override
  public List<Product> findCursorPage(
      String tenantId,
      ProductStatus status,
      Long categoryId,
      String keyword,
      Instant afterCreatedAt,
      Long afterId,
      int limit) {

    // ── Full-text search path ────────────────────────────────────────────────
    if (keyword != null && !keyword.isBlank()) {
      return afterCreatedAt == null
          ? jpaRepo.searchFirstPage(tenantId, keyword, limit)
          : jpaRepo.searchNextPage(tenantId, keyword, afterCreatedAt, afterId, limit);
    }

    // ── Filter-only paths ────────────────────────────────────────────────────
    var pageable = PageRequest.of(0, limit);

    if (status == null && categoryId == null) {
      return afterCreatedAt == null
          ? jpaRepo.findFirstPage(tenantId, pageable)
          : jpaRepo.findNextPage(tenantId, afterCreatedAt, afterId, pageable);
    }
    if (status != null && categoryId == null) {
      return afterCreatedAt == null
          ? jpaRepo.findFirstPageByStatus(tenantId, status, pageable)
          : jpaRepo.findNextPageByStatus(tenantId, status, afterCreatedAt, afterId, pageable);
    }
    if (status == null) {
      return afterCreatedAt == null
          ? jpaRepo.findFirstPageByCategory(tenantId, categoryId, pageable)
          : jpaRepo.findNextPageByCategory(tenantId, categoryId, afterCreatedAt, afterId, pageable);
    }
    return afterCreatedAt == null
        ? jpaRepo.findFirstPageByStatusAndCategory(tenantId, status, categoryId, pageable)
        : jpaRepo.findNextPageByStatusAndCategory(
            tenantId, status, categoryId, afterCreatedAt, afterId, pageable);
  }
}
