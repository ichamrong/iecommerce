package com.chamrong.iecommerce.auth.infrastructure.otp;

import java.time.Instant;

/**
 * Immutable snapshot of a stored OTP code.
 *
 * @param code The 6-digit numeric code.
 * @param expiresAt When the code stops being valid.
 * @param used Whether the code has already been consumed (one-time use).
 * @param purpose The purpose this OTP was issued for (e.g. EMAIL_VERIFY, STEP_UP).
 */
public record OtpEntry(String code, Instant expiresAt, boolean used, String purpose) {

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isValid(final String submittedCode) {
    return !used && !isExpired() && code.equals(submittedCode);
  }

  public OtpEntry markUsed() {
    return new OtpEntry(code, expiresAt, true, purpose);
  }
}
