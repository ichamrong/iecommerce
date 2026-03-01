package com.chamrong.iecommerce.booking.domain.model;

/**
 * Origin of the booking: POS, WEB, OTA, etc. Used for reporting and policy.
 */
public enum BookingSource {
  POS,
  WEB,
  OTA,
  MOBILE,
  API,
  MANUAL
}
