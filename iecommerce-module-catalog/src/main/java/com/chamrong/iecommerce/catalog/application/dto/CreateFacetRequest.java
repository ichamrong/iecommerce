package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

public record CreateFacetRequest(
    String code, boolean filterable, Map<String, String> translationNames) {}
