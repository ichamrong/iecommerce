package com.chamrong.iecommerce.catalog.application;

import com.chamrong.iecommerce.catalog.CatalogApi;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import com.chamrong.iecommerce.catalog.domain.ProductVariant;
import com.chamrong.iecommerce.catalog.domain.ProductVariantRepository;
import com.chamrong.iecommerce.catalog.domain.ProductVariantTranslation;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogApi {

  private final ProductVariantRepository variantRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<ProductVariantInfo> findActiveVariant(Long variantId) {
    return variantRepository
        .findById(variantId)
        .filter(v -> v.getProduct().getStatus() == ProductStatus.ACTIVE)
        .filter(ProductVariant::isEnabled)
        .map(
            v -> {
              // Resolve name to "en" locally for cross-module usage by default
              var resolvedName =
                  v.getTranslations().stream()
                      .filter(t -> t.getLocale().equals("en"))
                      .map(ProductVariantTranslation::getName)
                      .findFirst()
                      .orElse(
                          v.getProduct().getSlug()); // Fallback to product slug if no translation

              return new ProductVariantInfo(
                  v.getId(),
                  v.getProduct().getId(),
                  v.getSku(),
                  resolvedName,
                  v.getPrice() != null ? v.getPrice().getAmount() : null,
                  v.getPrice() != null ? v.getPrice().getCurrency() : null,
                  v.getProduct().getStatus(),
                  v.isEnabled());
            });
  }

  @Override
  @Transactional(readOnly = true)
  public int getStockLevel(Long variantId) {
    return variantRepository.findById(variantId).map(ProductVariant::getStockLevel).orElse(0);
  }

  @Override
  @Transactional
  public void updateStockLevel(Long variantId, int newStockLevel) {
    variantRepository
        .findById(variantId)
        .ifPresent(
            v -> {
              v.setStockLevel(newStockLevel);
              variantRepository.save(v);
            });
  }
}
