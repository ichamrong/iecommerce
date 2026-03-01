package com.chamrong.iecommerce.catalog.domain.ports;

import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Port for product persistence. Implementations live in infrastructure/persistence.
 *
 * <p>Application layer uses this port only; no direct dependency on Spring Data or JPA.
 */
public interface ProductRepositoryPort {

  Optional<Product> findById(Long id);

  Optional<Product> findByTenantIdAndSlug(String tenantId, String slug);

  /** Finds product that has a variant with the given SKU (tenant-scoped). */
  Optional<Product> findByTenantIdAndVariantSku(String tenantId, String sku);

  /** Finds product by barcode (tenant-scoped). */
  Optional<Product> findByTenantIdAndBarcode(String tenantId, String barcode);

  boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId);

  long countByTenantId(String tenantId);

  Product save(Product product);

  void delete(Product product);

  /**
   * Keyset paginated query with optional filters and keyword search.
   *
   * @param tenantId required tenant scope
   * @param status optional status filter
   * @param categoryId optional category filter
   * @param keyword optional full-text keyword
   * @param afterCreatedAt cursor; null → first page
   * @param afterId cursor tie-break; null → first page
   * @param limit max rows (request limit+1 to detect hasNext)
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
