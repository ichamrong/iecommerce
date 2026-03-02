package com.chamrong.iecommerce.review.domain.model;

import java.time.Instant;

/** Half-open time window {@code [from, to]} used for editability and abuse checks. */
public record TimeWindow(Instant from, Instant to) {

  public TimeWindow {
    if (from == null || to == null) {
      throw new IllegalArgumentException("from and to must not be null");
    }
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("to must be >= from");
    }
  }

  public boolean contains(Instant instant) {
    if (instant == null) {
      return false;
    }
    return !instant.isBefore(from) && !instant.isAfter(to);
  }
}
