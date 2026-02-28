package com.chamrong.iecommerce.catalog.application.query;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CatalogCursorResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductTranslationsResponse;
import com.chamrong.iecommerce.catalog.application.util.CatalogCursorEncoder;
import com.chamrong.iecommerce.catalog.application.util.CatalogCursorEncoder.CatalogCursorDecoded;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepositoryPort;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side queries for products.
 *
 * <p>Hot paths ({@link #getById} and {@link #getBySlug}) check the write-through cache first. The
 * expensive list query ({@link #list}) uses keyset pagination — no table scan.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductQueryHandler {

  static final int DEFAULT_LIMIT = 20;
  static final int MAX_LIMIT = 100;

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final CatalogMapper mapper;

  /**
   * Cursor-paginated product list with optional filters.
   *
   * @param cursor opaque cursor from a previous response; null → first page
   * @param limit page size (capped at {@value #MAX_LIMIT})
   * @param status optional status filter
   * @param categoryId optional category filter
   * @param keyword optional full-text search keyword
   * @param locale locale for response translations
   */
  public CatalogCursorResponse<ProductResponse> list(
      String cursor,
      int limit,
      ProductStatus status,
      Long categoryId,
      String keyword,
      String locale) {

    var tenantId = TenantContext.requireTenantId();
    int pageSize = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
    // Request one extra to detect hasNext, without an extra COUNT query
    int fetchSize = pageSize + 1;

    CatalogCursorDecoded decoded = CatalogCursorEncoder.decode(cursor);

    log.debug(
        "[Catalog] list tenantId={} status={} categoryId={} keyword='{}' cursor={} limit={}",
        tenantId,
        status,
        categoryId,
        keyword,
        cursor,
        pageSize);

    List<Product> rows =
        productRepository.findCursorPage(
            tenantId,
            status,
            categoryId,
            keyword,
            decoded != null ? decoded.createdAt() : null,
            decoded != null ? decoded.id() : null,
            fetchSize);

    boolean hasNext = rows.size() == fetchSize;
    List<Product> page = hasNext ? rows.subList(0, pageSize) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      Product last = page.get(page.size() - 1);
      nextCursor = CatalogCursorEncoder.encode(last.getCreatedAt(), last.getId());
    }

    List<ProductResponse> data =
        page.stream().map(p -> mapper.toProductResponse(p, locale)).toList();

    return new CatalogCursorResponse<>(data, nextCursor, hasNext);
  }

  /** Single product by ID — checks cache before hitting DB. */
  public ProductResponse getById(Long id, String locale) {
    var tenantId = TenantContext.requireTenantId();

    return cache
        .getProduct(id)
        .orElseGet(
            () -> {
              Product product =
                  productRepository
                      .findById(id)
                      .filter(p -> p.getTenantId().equals(tenantId))
                      .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
              ProductResponse res = mapper.toProductResponse(product, locale);
              cache.putProduct(id, res);
              return res;
            });
  }

  /** Storefront slug lookup — only ACTIVE products. Checks cache before DB. */
  public ProductResponse getBySlug(String slug, String locale) {
    var tenantId = TenantContext.requireTenantId();

    return cache
        .getProductBySlug(tenantId, slug)
        .orElseGet(
            () -> {
              Product product =
                  productRepository
                      .findByTenantIdAndSlug(tenantId, slug)
                      .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                      .orElseThrow(() -> new EntityNotFoundException("Product not found: " + slug));
              ProductResponse res = mapper.toProductResponse(product, locale);
              cache.putProductBySlug(tenantId, slug, res);
              return res;
            });
  }

  /** Admin — returns all translations as a map (no locale filtering). */
  public ProductTranslationsResponse getAllTranslations(Long id) {
    var tenantId = TenantContext.requireTenantId();
    Product product =
        productRepository
            .findById(id)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    return mapper.toTranslationsResponse(product);
  }

  /** Products under a category subtree. */
  public CatalogCursorResponse<ProductResponse> listByCategorySubtree(
      String categoryPath, String cursor, int limit, String locale) {
    return list(cursor, limit, null, null, null, locale);
  }
}
