package com.chamrong.iecommerce.inventory.domain;

import java.time.Instant;

/**
 * Domain abstraction for the current time. Allows handlers to be tested with deterministic clocks
 * without any Mockito clock-static hacks.
 *
 * <p>Default implementation: {@code SystemClockAdapter} (returns {@code Instant.now()}). Test
 * implementation: fixed or mutable fake clock.
 */
public interface ClockPort {

  /** Returns the current instant. Must never return null. */
  Instant now();
}
