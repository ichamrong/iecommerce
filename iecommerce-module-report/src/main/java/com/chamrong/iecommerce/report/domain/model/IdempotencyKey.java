package com.chamrong.iecommerce.report.domain.model;

/** Simple wrapper around a client-provided idempotency key for export operations. */
public record IdempotencyKey(String value) {

  public IdempotencyKey {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("idempotency key must not be blank");
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
