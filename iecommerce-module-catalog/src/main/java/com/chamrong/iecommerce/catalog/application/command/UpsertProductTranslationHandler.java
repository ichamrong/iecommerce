package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest.TranslationRequest;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Upserts a single locale's translation for a product. */
@Service
@Transactional
@RequiredArgsConstructor
public class UpsertProductTranslationHandler {

  private final ProductRepository productRepository;

  public void handle(Long productId, String locale, TranslationRequest req) {
    var tenantId = TenantContext.requireTenantId();
    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    product.upsertTranslation(
        locale,
        req.name(),
        req.description(),
        req.shortDescription(),
        req.metaTitle(),
        req.metaDescription());
    productRepository.save(product);
  }

  /** Deletes a locale translation. Cannot delete "en" (platform baseline). */
  @Transactional
  public void delete(Long productId, String locale) {
    if ("en".equals(locale)) {
      throw new IllegalArgumentException("Cannot delete the 'en' baseline translation.");
    }
    var tenantId = TenantContext.requireTenantId();
    var product =
        productRepository
            .findById(productId)
            .filter(p -> p.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

    product.getTranslations().stream()
        .filter(t -> t.getLocale().equals(locale))
        .findFirst()
        .ifPresent(
            t -> {
              // Trigger orphanRemoval by removing from the collection via the aggregate
              // (we access the mutable internal list through a package-level method)
              product.removeTranslation(locale);
            });
    productRepository.save(product);
  }
}
