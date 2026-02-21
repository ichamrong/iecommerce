package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

public record UpdateCollectionRequest(
    String slug,
    Boolean automatic,
    String rule,
    Integer sortOrder,
    Boolean active,
    Map<String, CreateCollectionRequest.CollectionTranslationDto> translations) {}
