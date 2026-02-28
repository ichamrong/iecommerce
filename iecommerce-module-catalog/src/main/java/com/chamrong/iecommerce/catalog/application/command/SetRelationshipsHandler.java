package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductRelationshipsUpdatedEvent;
import com.chamrong.iecommerce.catalog.application.dto.SetRelationshipsRequest;
import com.chamrong.iecommerce.catalog.domain.ProductRelationship;
import com.chamrong.iecommerce.catalog.domain.ProductRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SetRelationshipsHandler {

  private final ProductRepositoryPort productRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long productId, List<SetRelationshipsRequest> relationships) {
    var tenantId = TenantContext.requireTenantId();

    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    // Clear existing relationships and recreate
    product.getRelationships().clear();

    if (relationships != null) {
      relationships.forEach(
          req -> {
            var relatedProduct =
                productRepository
                    .findById(req.relatedProductId())
                    .filter(rp -> rp.getTenantId().equals(tenantId))
                    .orElseThrow(
                        () ->
                            new EntityNotFoundException(
                                "Related product not found: " + req.relatedProductId()));

            var relationship = new ProductRelationship(product, relatedProduct, req.type());
            relationship.setSortOrder(req.sortOrder());

            product.getRelationships().add(relationship);
          });
    }

    productRepository.save(product);
    eventPublisher.publishEvent(new ProductRelationshipsUpdatedEvent(tenantId, productId));
  }
}
