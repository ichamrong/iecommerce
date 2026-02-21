package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.VariantRemovedEvent;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RemoveVariantHandler {

  private final ProductRepository productRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long productId, Long variantId) {
    var tenantId = TenantContext.requireTenantId();

    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    boolean removed = product.getVariants().removeIf(v -> v.getId().equals(variantId));
    if (!removed) {
      throw new EntityNotFoundException("Variant not found: " + variantId);
    }

    productRepository.save(product);
    eventPublisher.publishEvent(new VariantRemovedEvent(tenantId, productId, variantId));
  }
}
