package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductPublishedEvent;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transitions a DRAFT or ARCHIVED product → ACTIVE and publishes {@code ProductPublishedEvent}. */
@Service
@Transactional
@RequiredArgsConstructor
public class PublishProductHandler {

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long productId) {
    var tenantId = TenantContext.requireTenantId();
    var product = findOwned(productId, tenantId);
    product.publish();
    productRepository.save(product);
    cache.evictProduct(productId);
    eventPublisher.publishEvent(new ProductPublishedEvent(tenantId, product.getId()));
  }

  private Product findOwned(Long id, String tenantId) {
    return productRepository
        .findById(id)
        .filter(p -> p.getTenantId().equals(tenantId))
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
  }
}
