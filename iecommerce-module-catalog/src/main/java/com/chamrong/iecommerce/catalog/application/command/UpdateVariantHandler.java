package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.VariantUpdatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.UpdateVariantRequest;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
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
public class UpdateVariantHandler {

  private final ProductRepository productRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public ProductResponse handle(
      Long productId, Long variantId, UpdateVariantRequest request, String locale) {
    var tenantId = TenantContext.requireTenantId();

    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    var variant =
        product.getVariants().stream()
            .filter(v -> v.getId().equals(variantId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Variant not found: " + variantId));

    if (request.priceAmount() != null && request.priceCurrency() != null) {
      variant.setPrice(Money.of(request.priceAmount(), request.priceCurrency()));
    }
    if (request.compareAtPriceAmount() != null && request.compareAtPriceCurrency() != null) {
      variant.setCompareAtPrice(
          Money.of(request.compareAtPriceAmount(), request.compareAtPriceCurrency()));
    }
    if (request.weightGrams() != null) variant.setWeightGrams(request.weightGrams());
    if (request.enabled() != null) variant.setEnabled(request.enabled());
    if (request.sortOrder() != null) variant.setSortOrder(request.sortOrder());

    if (request.translationNames() != null) {
      request.translationNames().forEach(variant::upsertTranslation);
    }

    var saved = productRepository.save(product);
    eventPublisher.publishEvent(new VariantUpdatedEvent(tenantId, productId, variantId));
    return catalogMapper.toProductResponse(saved, locale);
  }
}
