package com.chamrong.iecommerce.report.domain.model;

import java.time.Instant;

/**
 * Immutable time range value object used to scope reports.
 *
 * <p>Represents a half-open interval {@code [from, to]} in UTC.
 */
public record TimeRange(Instant from, Instant to) {

  public TimeRange {
    if (from == null || to == null) {
      throw new IllegalArgumentException("from and to must not be null");
    }
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("to must be >= from");
    }
  }
}
