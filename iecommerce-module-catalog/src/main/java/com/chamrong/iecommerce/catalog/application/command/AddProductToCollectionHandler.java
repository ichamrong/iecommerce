package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductAddedToCollectionEvent;
import com.chamrong.iecommerce.catalog.domain.CollectionRepository;
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
public class AddProductToCollectionHandler {

  private final CollectionRepository collectionRepository;
  private final ProductRepository productRepository;
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
      throw new IllegalStateException("Cannot manually add product to an automatic collection");
    }

    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    collection.addProduct(product);
    collectionRepository.save(collection);
    eventPublisher.publishEvent(
        new ProductAddedToCollectionEvent(tenantId, collectionId, productId));
  }
}
