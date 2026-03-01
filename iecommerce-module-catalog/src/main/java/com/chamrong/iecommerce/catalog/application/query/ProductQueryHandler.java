package com.chamrong.iecommerce.catalog.application.query;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductTranslationsResponse;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  /** Endpoint key for filterHash binding (product list). */
  public static final String ENDPOINT_LIST_PRODUCTS = "catalog:listProducts";

  static final int DEFAULT_LIMIT = 20;
  static final int MAX_LIMIT = 100;

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final CatalogMapper mapper;

  /**
   * Cursor-paginated product list with optional filters. Uses shared CursorCodec and FilterHasher;
   * cursor from a different filter set is rejected with INVALID_CURSOR_FILTER_MISMATCH.
   *
   * @param cursor opaque cursor from a previous response; null/blank → first page
   * @param limit page size (capped at {@value #MAX_LIMIT})
   * @param status optional status filter
   * @param categoryId optional category filter
   * @param keyword optional full-text search keyword
   * @param locale locale for response translations
   */
  public CursorPageResponse<ProductResponse> list(
      String cursor,
      int limit,
      ProductStatus status,
      Long categoryId,
      String keyword,
      String locale) {

    var tenantId = TenantContext.requireTenantId();
    int effectiveLimit = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
    int fetchSize = effectiveLimit + 1;

    Map<String, Object> filterMap = toFilterMap(status, categoryId, keyword);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_PRODUCTS, filterMap);

    Instant afterCreatedAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      afterCreatedAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    log.debug(
        "[Catalog] list tenantId={} status={} categoryId={} keyword='{}' cursor={} limit={}",
        tenantId,
        status,
        categoryId,
        keyword,
        cursor,
        effectiveLimit);

    List<Product> rows =
        productRepository.findCursorPage(
            tenantId, status, categoryId, keyword, afterCreatedAt, afterId, fetchSize);

    boolean hasNext = rows.size() == fetchSize;
    List<Product> page = hasNext ? rows.subList(0, effectiveLimit) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      Product last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    List<ProductResponse> data =
        page.stream().map(p -> mapper.toProductResponse(p, locale)).toList();

    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

  /** Builds filter map for FilterHasher from list parameters. */
  public static Map<String, Object> toFilterMap(
      ProductStatus status, Long categoryId, String keyword) {
    Map<String, Object> m = new LinkedHashMap<>();
    if (status != null) m.put("status", status.name());
    if (categoryId != null) m.put("categoryId", categoryId);
    if (keyword != null && !keyword.isBlank()) m.put("keyword", keyword);
    return m;
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
                      .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
              TenantGuard.requireSameTenant(product.getTenantId(), tenantId);
              ProductResponse res = mapper.toProductResponse(product, locale);
              cache.putProduct(id, res);
              return res;
            });
  }

  /** Lookup by variant SKU (tenant-scoped). For POS fast scan. */
  public ProductResponse getBySku(String sku, String locale) {
    var tenantId = TenantContext.requireTenantId();
    Product product =
        productRepository
            .findByTenantIdAndVariantSku(tenantId, sku)
            .orElseThrow(() -> new EntityNotFoundException("Product not found for SKU: " + sku));
    return mapper.toProductResponse(product, locale);
  }

  /** Lookup by barcode (tenant-scoped). For POS fast scan. */
  public ProductResponse getByBarcode(String barcode, String locale) {
    var tenantId = TenantContext.requireTenantId();
    Product product =
        productRepository
            .findByTenantIdAndBarcode(tenantId, barcode)
            .orElseThrow(() -> new EntityNotFoundException("Product not found for barcode: " + barcode));
    return mapper.toProductResponse(product, locale);
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
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    TenantGuard.requireSameTenant(product.getTenantId(), tenantId);
    return mapper.toTranslationsResponse(product);
  }

  /** Products under a category subtree. */
  public CursorPageResponse<ProductResponse> listByCategorySubtree(
      String categoryPath, String cursor, int limit, String locale) {
    return list(cursor, limit, null, null, null, locale);
  }
}
