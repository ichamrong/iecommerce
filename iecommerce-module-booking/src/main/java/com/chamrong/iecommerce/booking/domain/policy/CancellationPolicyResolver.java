package com.chamrong.iecommerce.booking.domain.policy;

import com.chamrong.iecommerce.booking.domain.model.CancellationPolicy;
import com.chamrong.iecommerce.booking.domain.model.Booking;
import java.time.Instant;

/**
 * Resolves cancellation policy for a booking (e.g. from resource config, tenant settings).
 */
public interface CancellationPolicyResolver {

  /**
   * Returns the cancellation policy for the given booking.
   *
   * @param booking the booking
   * @return policy (never null)
   */
  CancellationPolicy resolve(Booking booking);
}
