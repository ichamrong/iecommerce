package com.chamrong.iecommerce.auth.application.command.auth.otp;

import com.chamrong.iecommerce.auth.application.exception.OtpVerificationException;
import com.chamrong.iecommerce.auth.infrastructure.otp.OtpStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Verifies a submitted one-time password (OTP) code.
 *
 * <h3>Security properties</h3>
 *
 * <ul>
 *   <li>Consumes the code on first valid use — replay attacks return {@code false}.
 *   <li>Expired codes return {@code false} — clock skew is not tolerated.
 *   <li>Wrong codes simply fail; no timing oracle exists since both branches take the same code
 *       path in {@link OtpStore#verifyAndConsume}.
 *   <li>Failure cases are always logged at {@code WARN} for security monitoring.
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyOtpHandler {

  private final OtpStore otpStore;

  /**
   * Verifies the submitted code and marks it as consumed if valid.
   *
   * @param command the verify OTP command
   * @throws OtpVerificationException if the code is wrong, expired, or already used
   */
  public void handle(@NonNull final VerifyOtpCommand command) {
    final boolean valid =
        otpStore.verifyAndConsume(command.userId(), command.purpose(), command.submittedCode());

    if (!valid) {
      log.warn(
          "OTP verification failed for userId={} purpose={}", command.userId(), command.purpose());
      throw new OtpVerificationException("Invalid, expired, or already used OTP code");
    }

    log.info(
        "OTP verified successfully for userId={} purpose={}", command.userId(), command.purpose());
  }
}
