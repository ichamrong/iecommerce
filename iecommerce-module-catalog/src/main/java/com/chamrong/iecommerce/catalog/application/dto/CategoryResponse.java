package com.chamrong.iecommerce.catalog.application.dto;

import java.util.List;

/** Flat category node used in tree responses. */
public record CategoryResponse(
    Long id,
    String slug,
    Long parentId,
    String materializedPath,
    int depth,
    int sortOrder,
    String imageUrl,
    boolean active,
    String resolvedLocale,
    String name,
    String description,
    List<CategoryResponse> children // null unless tree endpoint is used
    ) {}
