package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductUpdatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.UpdateProductRequest;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates mutable fields of an existing product. Translations are upserted via the map in the
 * request — absent locales are left untouched. Lifecycle status (publish/archive) has dedicated
 * handlers.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateProductHandler {

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final CatalogMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  public ProductResponse handle(Long productId, UpdateProductRequest req, String locale) {
    var tenantId = TenantContext.requireTenantId();
    var product = findOwned(productId, tenantId);

    if (req.basePriceAmount() != null) {
      product.setBasePrice(Money.of(req.basePriceAmount(), req.basePriceCurrency()));
    }
    if (req.compareAtPriceAmount() != null) {
      product.setCompareAtPrice(Money.of(req.compareAtPriceAmount(), req.compareAtPriceCurrency()));
    }
    if (req.categoryId() != null) product.setCategoryId(req.categoryId());
    if (req.taxCategory() != null) product.setTaxCategory(req.taxCategory());
    if (req.tags() != null) product.setTags(req.tags());
    if (req.serviceDurationMinutes() != null)
      product.setServiceDurationMinutes(req.serviceDurationMinutes());
    if (req.requiredStaffCount() != null) product.setRequiredStaffCount(req.requiredStaffCount());

    if (req.translations() != null) {
      req.translations()
          .forEach(
              (l, t) ->
                  product.upsertTranslation(
                      l,
                      t.name(),
                      t.description(),
                      t.shortDescription(),
                      t.metaTitle(),
                      t.metaDescription()));
    }

    var saved = productRepository.save(product);
    cache.evictProduct(saved.getId());
    eventPublisher.publishEvent(new ProductUpdatedEvent(tenantId, saved.getId()));
    return mapper.toProductResponse(saved, locale);
  }

  private Product findOwned(Long id, String tenantId) {
    return productRepository
        .findById(id)
        .filter(p -> p.getTenantId().equals(tenantId))
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
  }
}
