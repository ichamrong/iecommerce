package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.ProductCreatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepositoryPort;
import com.chamrong.iecommerce.catalog.domain.ProductType;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.subscription.SubscriptionApi;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the creation of a new {@link Product} including all translations and variants.
 *
 * <p>Steps:
 *
 * <ol>
 *   <li>Enforce tenant product quota limit
 *   <li>Validate "en" translation is present
 *   <li>Auto-generate slug from English name if not supplied
 *   <li>Check slug uniqueness per tenant
 *   <li>Build and persist the Product aggregate
 *   <li>Return the resolved response (defaults to "en")
 * </ol>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreateProductHandler {

  private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

  private final ProductRepositoryPort productRepository;
  private final CatalogCachePort cache;
  private final CatalogMapper mapper;
  private final ApplicationEventPublisher eventPublisher;
  private final SubscriptionApi subscriptionApi;

  public ProductResponse handle(CreateProductRequest req) {
    var tenantId = TenantContext.requireTenantId();

    // 0. Enforce quota
    long currentCount = productRepository.countByTenantId(tenantId);
    subscriptionApi.checkQuota(tenantId, "maxProducts", currentCount);

    // 1. Validate at least "en" translation
    validateTranslations(req.translations());

    // 2. Resolve slug
    var slug = resolveSlug(tenantId, req);

    // 3. Build aggregate
    ProductType type = ProductType.valueOf(req.productType());
    Money basePrice =
        req.basePriceAmount() != null
            ? Money.of(req.basePriceAmount(), req.basePriceCurrency())
            : null;

    var product = new Product(tenantId, slug, type, basePrice);
    product.setTaxCategory(req.taxCategory() != null ? req.taxCategory() : "STANDARD");
    product.setTags(req.tags());
    product.setCategoryId(req.categoryId());
    product.setServiceDurationMinutes(req.serviceDurationMinutes());
    product.setRequiredStaffCount(req.requiredStaffCount());

    if (req.compareAtPriceAmount() != null) {
      product.setCompareAtPrice(Money.of(req.compareAtPriceAmount(), req.compareAtPriceCurrency()));
    }

    // 4. Apply translations
    req.translations()
        .forEach(
            (locale, t) ->
                product.upsertTranslation(
                    locale,
                    t.name(),
                    t.description(),
                    t.shortDescription(),
                    t.metaTitle(),
                    t.metaDescription()));

    // 5. Apply variants
    if (req.variants() != null) {
      req.variants()
          .forEach(
              vReq -> {
                Money vPrice =
                    vReq.priceAmount() != null
                        ? Money.of(vReq.priceAmount(), vReq.priceCurrency())
                        : basePrice;
                var variant = product.addVariant(vReq.sku(), vPrice);
                variant.setSortOrder(vReq.sortOrder());
                if (vReq.weightGrams() != null) variant.setWeightGrams(vReq.weightGrams());
                if (vReq.translations() != null) {
                  vReq.translations().forEach(variant::upsertTranslation);
                }
              });
    }

    var saved = productRepository.save(product);
    ProductResponse res = mapper.toProductResponse(saved, "en");
    cache.putProduct(saved.getId(), res);
    eventPublisher.publishEvent(new ProductCreatedEvent(tenantId, saved.getId()));
    return res;
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void validateTranslations(
      Map<String, CreateProductRequest.TranslationRequest> translations) {
    if (translations == null || translations.isEmpty()) {
      throw new IllegalArgumentException("At least one translation is required.");
    }
    if (!translations.containsKey("en")) {
      throw new IllegalArgumentException(
          "English ('en') translation is always required as the platform baseline.");
    }
    if (translations.get("en").name() == null || translations.get("en").name().isBlank()) {
      throw new IllegalArgumentException("English product name cannot be blank.");
    }
  }

  private String resolveSlug(String tenantId, CreateProductRequest req) {
    String slug =
        req.slug() != null && !req.slug().isBlank()
            ? req.slug()
            : toSlug(req.translations().get("en").name());

    // Uniqueness check
    if (productRepository.existsByTenantIdAndSlugAndIdNot(tenantId, slug, -1L)) {
      throw new IllegalArgumentException("Slug '" + slug + "' is already in use for this tenant.");
    }
    return slug;
  }

  /** Converts "Samsung Galaxy S25" → "samsung-galaxy-s25". */
  static String toSlug(String input) {
    String normalized =
        Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    return NON_ALPHANUMERIC
        .matcher(normalized.toLowerCase(Locale.ROOT))
        .replaceAll("-")
        .replaceAll("^-+|-+$", "");
  }
}
