package com.chamrong.iecommerce.customer.domain.ports;

import com.chamrong.iecommerce.customer.domain.model.CustomerStatus;
import java.time.Instant;

/** Criteria for customer list/search. Used for keyset pagination and filter hashing. */
public final class CustomerSearchCriteria {

  private final CustomerStatus status;
  private final String search;
  private final Instant createdAtFrom;
  private final Instant createdAtTo;

  public CustomerSearchCriteria(
      CustomerStatus status, String search, Instant createdAtFrom, Instant createdAtTo) {
    this.status = status;
    this.search = search;
    this.createdAtFrom = createdAtFrom;
    this.createdAtTo = createdAtTo;
  }

  public static CustomerSearchCriteria empty() {
    return new CustomerSearchCriteria(null, null, null, null);
  }

  public CustomerStatus getStatus() {
    return status;
  }

  public String getSearch() {
    return search;
  }

  public Instant getCreatedAtFrom() {
    return createdAtFrom;
  }

  public Instant getCreatedAtTo() {
    return createdAtTo;
  }
}
