package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

public record CreateCollectionRequest(
    String slug,
    boolean automatic,
    String rule,
    int sortOrder,
    boolean active,
    Map<String, CollectionTranslationDto> translations) {

  public record CollectionTranslationDto(String name, String description) {}
}
