package com.chamrong.iecommerce.catalog.application;

import com.chamrong.iecommerce.catalog.application.dto.CategoryResponse;
import com.chamrong.iecommerce.catalog.application.dto.CollectionResponse;
import com.chamrong.iecommerce.catalog.application.dto.FacetResponse;
import com.chamrong.iecommerce.catalog.application.dto.FacetResponse.FacetValueResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse.VariantResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductTranslationsResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductTranslationsResponse.TranslationEntry;
import com.chamrong.iecommerce.catalog.domain.Category;
import com.chamrong.iecommerce.catalog.domain.CategoryTranslation;
import com.chamrong.iecommerce.catalog.domain.Collection;
import com.chamrong.iecommerce.catalog.domain.CollectionTranslation;
import com.chamrong.iecommerce.catalog.domain.Facet;
import com.chamrong.iecommerce.catalog.domain.FacetTranslation;
import com.chamrong.iecommerce.catalog.domain.FacetValueTranslation;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductVariant;
import jakarta.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Converts domain objects → DTOs, applying locale resolution.
 *
 * <p>Locale fallback chain: requested locale → "en" → first available. The resolved locale is
 * always included in the response so callers know which was used.
 */
@Component
public class CatalogMapper {

  // ── Product ───────────────────────────────────────────────────────────────

  public ProductResponse toProductResponse(Product product, String requestedLocale) {
    var t = resolveTranslation(product.getTranslations(), requestedLocale);
    var resolvedLocale = t != null ? t.getLocale() : "en";

    var variantResponses =
        product.getVariants().stream().map(v -> toVariantResponse(v, requestedLocale)).toList();

    return new ProductResponse(
        product.getId(),
        product.getSlug(),
        product.getStatus().name(),
        product.getProductType().name(),
        product.getCategoryId(),
        product.getBasePrice().getAmount(),
        product.getBasePrice().getCurrency(),
        product.getCompareAtPrice().getAmount(),
        product.getCompareAtPrice().getCurrency(),
        product.getTaxCategory(),
        product.getTags(),
        product.getServiceDurationMinutes(),
        product.getRequiredStaffCount(),
        resolvedLocale,
        t != null ? t.getName() : null,
        t != null ? t.getDescription() : null,
        t != null ? t.getShortDescription() : null,
        t != null ? t.getMetaTitle() : null,
        t != null ? t.getMetaDescription() : null,
        variantResponses);
  }

  public ProductTranslationsResponse toTranslationsResponse(Product product) {
    Map<String, TranslationEntry> map = new LinkedHashMap<>();
    for (var t : product.getTranslations()) {
      map.put(
          t.getLocale(),
          new TranslationEntry(
              t.getName(),
              t.getDescription(),
              t.getShortDescription(),
              t.getMetaTitle(),
              t.getMetaDescription()));
    }
    return new ProductTranslationsResponse(product.getId(), product.getSlug(), map);
  }

  private VariantResponse toVariantResponse(ProductVariant v, String locale) {
    var t =
        v.getTranslations().stream()
            .filter(vt -> vt.getLocale().equals(locale))
            .findFirst()
            .orElse(
                v.getTranslations().stream()
                    .filter(vt -> vt.getLocale().equals("en"))
                    .findFirst()
                    .orElse(v.getTranslations().isEmpty() ? null : v.getTranslations().getFirst()));

    return new VariantResponse(
        v.getId(),
        v.getSku(),
        v.getPrice().getAmount(),
        v.getPrice().getCurrency(),
        v.getCompareAtPrice().getAmount(),
        v.getCompareAtPrice().getCurrency(),
        v.getWeightGrams(),
        v.getStockLevel(),
        v.isEnabled(),
        v.getSortOrder(),
        t != null ? t.getName() : null);
  }

  // ── Category ──────────────────────────────────────────────────────────────

  public CategoryResponse toCategoryResponse(Category category, String locale) {
    var t = (CategoryTranslation) resolveTranslation(category.getTranslations(), locale);
    return new CategoryResponse(
        category.getId(),
        category.getSlug(),
        category.getParentId(),
        category.getMaterializedPath(),
        category.getDepth(),
        category.getSortOrder(),
        category.getImageUrl(),
        category.isActive(),
        t != null ? t.getLocale() : "en",
        t != null ? t.getName() : null,
        t != null ? t.getDescription() : null,
        null // children populated by CategoryQueryHandler
        );
  }

  // ── Collection ────────────────────────────────────────────────────────────

  public CollectionResponse toCollectionResponse(Collection collection, String locale) {
    var t = (CollectionTranslation) resolveTranslation(collection.getTranslations(), locale);

    return new CollectionResponse(
        collection.getId(),
        collection.getSlug(),
        collection.isAutomatic(),
        collection.getRule(),
        collection.getSortOrder(),
        collection.isActive(),
        t != null ? t.getName() : null,
        t != null ? t.getDescription() : null,
        t != null ? t.getLocale() : "en");
  }

  // ── Facet ─────────────────────────────────────────────────────────────────

  public FacetResponse toFacetResponse(Facet facet, String locale) {
    var t = (FacetTranslation) resolveTranslation(facet.getTranslations(), locale);

    var valueResponses =
        facet.getValues().stream()
            .map(
                v -> {
                  var vt = (FacetValueTranslation) resolveTranslation(v.getTranslations(), locale);
                  return new FacetValueResponse(
                      v.getId(), vt != null ? vt.getValue() : null, v.getCode());
                })
            .toList();

    return new FacetResponse(
        facet.getId(),
        t != null ? t.getName() : null,
        facet.getCode(),
        facet.isFilterable(),
        t != null ? t.getLocale() : "en",
        valueResponses);
  }

  // ── Locale resolution ─────────────────────────────────────────────────────

  /**
   * Fallback chain: requested → "en" → first available. Works for any translation type that has a
   * getLocale() method.
   */
  @Nullable
  private <T> T resolveTranslation(List<T> translations, String requestedLocale) {
    if (translations.isEmpty()) return null;

    // Try requested
    var found = translations.stream().filter(t -> getLocale(t).equals(requestedLocale)).findFirst();
    if (found.isPresent()) return found.get();

    // Try en fallback
    found = translations.stream().filter(t -> getLocale(t).equals("en")).findFirst();
    return found.orElseGet(translations::getFirst);
  }

  @SuppressWarnings("unchecked")
  private <T> String getLocale(T t) {
    try {
      return (String) t.getClass().getMethod("getLocale").invoke(t);
    } catch (ReflectiveOperationException e) {
      return "";
    }
  }
}
