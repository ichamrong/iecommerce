package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

public record CreateCategoryRequest(
    String slug,
    Long parentId,
    int sortOrder,
    String imageUrl,
    boolean active,
    Map<String, CategoryTranslationRequest> translations) {

  public record CategoryTranslationRequest(String name, String description) {}
}
