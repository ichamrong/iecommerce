package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

public record UpdateCategoryRequest(
    String slug,
    Long parentId,
    Integer sortOrder,
    String imageUrl,
    Boolean active,
    Map<String, CreateCategoryRequest.CategoryTranslationRequest> translations) {}
