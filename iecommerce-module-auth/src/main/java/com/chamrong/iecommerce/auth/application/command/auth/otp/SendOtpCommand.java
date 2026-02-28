package com.chamrong.iecommerce.auth.application.command.auth.otp;

import org.springframework.lang.NonNull;

/**
 * Command to request a one-time password (OTP) be sent to the user's registered email.
 *
 * @param userId the authenticated user's local identifier
 * @param purpose the reason for the OTP request (e.g. {@code "EMAIL_VERIFY"}, {@code "STEP_UP"})
 */
public record SendOtpCommand(@NonNull String userId, @NonNull String purpose) {}
