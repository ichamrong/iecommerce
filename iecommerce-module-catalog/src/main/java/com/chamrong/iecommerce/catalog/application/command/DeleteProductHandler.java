package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductDeletedEvent;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Removes a product from the catalog (soft-delete). */
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteProductHandler {

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long productId) {
    var tenantId = TenantContext.requireTenantId();
    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    productRepository.delete(product);
    cache.evictProduct(productId);
    eventPublisher.publishEvent(new ProductDeletedEvent(tenantId, productId));
  }
}
