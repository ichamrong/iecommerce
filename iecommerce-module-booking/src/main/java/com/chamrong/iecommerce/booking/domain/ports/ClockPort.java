package com.chamrong.iecommerce.booking.domain.ports;

import java.time.Instant;

/**
 * Port for current time (testable). Default impl returns Instant.now().
 */
public interface ClockPort {

  Instant now();
}
