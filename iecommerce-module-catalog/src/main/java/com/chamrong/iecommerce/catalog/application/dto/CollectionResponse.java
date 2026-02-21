package com.chamrong.iecommerce.catalog.application.dto;

public record CollectionResponse(
    Long id,
    String slug,
    boolean automatic,
    String rule,
    int sortOrder,
    boolean active,
    String name,
    String description,
    String locale) {}
