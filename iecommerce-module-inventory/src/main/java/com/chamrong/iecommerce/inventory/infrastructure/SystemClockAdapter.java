package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.ClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Default implementation of {@link ClockPort} — delegates to system clock. */
@Component
public class SystemClockAdapter implements ClockPort {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
