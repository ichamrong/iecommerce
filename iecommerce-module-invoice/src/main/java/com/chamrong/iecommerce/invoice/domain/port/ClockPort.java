package com.chamrong.iecommerce.invoice.domain.port;

import java.time.Instant;

/**
 * Output port: deterministic clock access.
 *
 * <p>Using this port instead of {@code Instant.now()} or {@code Clock} directly allows application
 * use cases to be fully unit-tested without time-related flakiness.
 */
public interface ClockPort {

  /**
   * Returns the current instant.
   *
   * @return current UTC instant
   */
  Instant now();
}
