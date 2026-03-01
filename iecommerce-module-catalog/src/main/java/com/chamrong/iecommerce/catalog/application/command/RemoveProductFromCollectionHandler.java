package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductRemovedFromCollectionEvent;
import com.chamrong.iecommerce.catalog.domain.CollectionRepository;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RemoveProductFromCollectionHandler {

  private final CollectionRepository collectionRepository;
  private final ProductRepositoryPort productRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long collectionId, Long productId) {
    var tenantId = TenantContext.requireTenantId();

    var collection =
        collectionRepository
            .findById(collectionId)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(
                () -> new EntityNotFoundException("Collection not found: " + collectionId));

    if (collection.isAutomatic()) {
      throw new IllegalStateException(
          "Cannot manually remove product from an automatic collection");
    }

    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    collection.removeProduct(product);
    collectionRepository.save(collection);
    eventPublisher.publishEvent(
        new ProductRemovedFromCollectionEvent(tenantId, collectionId, productId));
  }
}
