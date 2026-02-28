package com.chamrong.iecommerce.order.infrastructure.util;

import com.chamrong.iecommerce.order.domain.ports.ClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Standard system clock implementation for production. */
@Component
public class SystemClockAdapter implements ClockPort {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
