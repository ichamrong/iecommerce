package com.chamrong.iecommerce.invoice.infrastructure.clock;

import com.chamrong.iecommerce.invoice.domain.ports.ClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** System clock adapter — returns the real UTC clock. Easily replaced in tests. */
@Component("invoiceSystemClockAdapter")
public class SystemClockAdapter implements ClockPort {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
