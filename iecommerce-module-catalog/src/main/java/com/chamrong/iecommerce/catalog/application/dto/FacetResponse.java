package com.chamrong.iecommerce.catalog.application.dto;

import java.util.List;

public record FacetResponse(
    Long id,
    String name,
    String code,
    boolean filterable,
    String locale,
    List<FacetValueResponse> values) {

  public record FacetValueResponse(Long id, String value, String code) {}
}
