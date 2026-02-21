package com.chamrong.iecommerce.catalog.application.query;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductTranslationsResponse;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side queries for products. All methods are read-only transactions. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryHandler {

  private final ProductRepository productRepository;
  private final CatalogMapper mapper;

  /** Admin list — all statuses. */
  public List<ProductResponse> listByTenant(String locale) {
    var tenantId = TenantContext.requireTenantId();
    return productRepository.findAll().stream()
        .filter(p -> p.getTenantId().equals(tenantId))
        .map(p -> mapper.toProductResponse(p, locale))
        .toList();
  }

  /** Admin list filtered by status. */
  public List<ProductResponse> listByStatus(ProductStatus status, String locale) {
    var tenantId = TenantContext.requireTenantId();
    return productRepository.findByTenantIdAndStatus(tenantId, status).stream()
        .map(p -> mapper.toProductResponse(p, locale))
        .toList();
  }

  /** Single product by ID — locale-resolved. */
  public ProductResponse getById(Long id, String locale) {
    var tenantId = TenantContext.requireTenantId();
    Product product =
        productRepository
            .findById(id)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    return mapper.toProductResponse(product, locale);
  }

  /** Storefront slug lookup — only ACTIVE products. */
  public ProductResponse getBySlug(String slug, String locale) {
    var tenantId = TenantContext.requireTenantId();
    Product product =
        productRepository
            .findByTenantIdAndSlug(tenantId, slug)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + slug));
    return mapper.toProductResponse(product, locale);
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
  public List<ProductResponse> listByCategorySubtree(String categoryPath, String locale) {
    var tenantId = TenantContext.requireTenantId();
    return productRepository.findByTenantIdAndCategorySubtree(tenantId, categoryPath + "%").stream()
        .map(p -> mapper.toProductResponse(p, locale))
        .toList();
  }
}
