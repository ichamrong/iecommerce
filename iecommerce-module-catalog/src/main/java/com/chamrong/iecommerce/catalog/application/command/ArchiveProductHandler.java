package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductArchivedEvent;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transitions ACTIVE → ARCHIVED and emits {@code ProductArchivedEvent}. */
@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveProductHandler {

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long productId) {
    var tenantId = TenantContext.requireTenantId();
    var product = findOwned(productId, tenantId);
    product.archive();
    productRepository.save(product);
    cache.evictProduct(productId);
    eventPublisher.publishEvent(new ProductArchivedEvent(tenantId, product.getId()));
  }

  private Product findOwned(Long id, String tenantId) {
    return productRepository
        .findById(id)
        .filter(p -> p.getTenantId().equals(tenantId))
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
  }
}
