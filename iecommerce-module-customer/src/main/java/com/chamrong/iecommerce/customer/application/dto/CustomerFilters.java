package com.chamrong.iecommerce.customer.application.dto;

import com.chamrong.iecommerce.customer.domain.model.CustomerStatus;

/** Filters for customer list endpoint. Used for cursor pagination and filterHash. */
public record CustomerFilters(
    CustomerStatus status,
    String search,
    java.time.Instant createdAtFrom,
    java.time.Instant createdAtTo) {

  public static CustomerFilters empty() {
    return new CustomerFilters(null, null, null, null);
  }
}
