package com.chamrong.iecommerce.report.domain.policy;

import com.chamrong.iecommerce.report.domain.model.TimeRange;
import java.time.Instant;

/** Simple date range specification used to scope report queries. */
public record DateRangeSpec(Instant from, Instant to) {

  public DateRangeSpec {
    if (from == null || to == null) {
      throw new IllegalArgumentException("from and to must not be null");
    }
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("to must be >= from");
    }
  }

  public TimeRange toTimeRange() {
    return new TimeRange(from, to);
  }
}
