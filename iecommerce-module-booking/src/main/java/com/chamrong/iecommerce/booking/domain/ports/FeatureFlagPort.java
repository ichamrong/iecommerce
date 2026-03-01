package com.chamrong.iecommerce.booking.domain.ports;

/**
 * Port for feature flag / subscription gating. Throws if accommodation/booking module not enabled.
 */
public interface FeatureFlagPort {

  /**
   * Ensures the booking/accommodation module is enabled for the tenant. Throws if not.
   *
   * @param tenantId current tenant
   * @throws com.chamrong.iecommerce.common.security.CapabilityDeniedException if disabled
   */
  void requireBookingEnabled(String tenantId);
}
