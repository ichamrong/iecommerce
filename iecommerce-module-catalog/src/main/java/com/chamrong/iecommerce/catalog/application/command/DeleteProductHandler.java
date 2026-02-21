package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductDeletedEvent;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
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

  private final ProductRepository productRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long productId) {
    var tenantId = TenantContext.requireTenantId();
    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    productRepository.delete(product);
    eventPublisher.publishEvent(new ProductDeletedEvent(tenantId, productId));
  }
}
