package com.chamrong.iecommerce.catalog.application.dto;

import com.chamrong.iecommerce.catalog.domain.RelationshipType;

public record SetRelationshipsRequest(
    Long relatedProductId, RelationshipType type, int sortOrder) {}
