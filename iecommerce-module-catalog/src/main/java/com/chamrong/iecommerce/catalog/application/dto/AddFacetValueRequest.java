package com.chamrong.iecommerce.catalog.application.dto;

import java.util.Map;

public record AddFacetValueRequest(String code, Map<String, String> translationValues) {}
