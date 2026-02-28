package com.chamrong.iecommerce.order.domain.ports;

import java.time.Instant;

/**
 * Clock abstraction — replaces {@code Instant.now()} calls in handlers.
 *
 * <p>Injecting the clock (rather than calling {@code Instant.now()} directly) makes handlers
 * deterministically testable with fixed-time clocks, avoiding fragile time-sensitive assertions in
 * unit tests.
 *
 * <p>Production implementation: {@code SystemClockAdapter} (returns {@code Instant.now()}). Test
 * implementation: fixed or mutable stub.
 */
public interface ClockPort {
  Instant now();
}
