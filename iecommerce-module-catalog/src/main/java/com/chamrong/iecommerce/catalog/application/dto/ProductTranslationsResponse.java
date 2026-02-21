package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

/** All translations for a product — used in admin "manage translations" view. */
public record ProductTranslationsResponse(
    Long id, String slug, Map<String, TranslationEntry> translations) {

  public record TranslationEntry(
      String name,
      String description,
      String shortDescription,
      String metaTitle,
      String metaDescription) {}
}
