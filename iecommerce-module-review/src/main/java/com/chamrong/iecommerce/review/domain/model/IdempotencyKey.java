package com.chamrong.iecommerce.review.domain.model;

/** Simple wrapper around a client-provided idempotency key. */
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
