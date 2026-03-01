package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.VariantAddedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.AddVariantRequest;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddVariantHandler {

  private final ProductRepositoryPort productRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public ProductResponse handle(Long productId, AddVariantRequest request, String locale) {
    var tenantId = TenantContext.requireTenantId();

    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    Money basePrice =
        (request.priceAmount() != null && request.priceCurrency() != null)
            ? Money.of(request.priceAmount(), request.priceCurrency())
            : null;
    assert basePrice != null;
    var variant = product.addVariant(request.sku(), basePrice);

    if (request.compareAtPriceAmount() != null && request.compareAtPriceCurrency() != null) {
      variant.setCompareAtPrice(
          Money.of(request.compareAtPriceAmount(), request.compareAtPriceCurrency()));
    }
    if (request.weightGrams() != null) variant.setWeightGrams(request.weightGrams());

    variant.setEnabled(request.enabled());
    variant.setSortOrder(request.sortOrder());

    if (request.translationNames() != null) {
      request.translationNames().forEach(variant::upsertTranslation);
    }

    var saved = productRepository.save(product);
    eventPublisher.publishEvent(new VariantAddedEvent(tenantId, productId, request.sku()));
    return catalogMapper.toProductResponse(saved, locale);
  }
}
