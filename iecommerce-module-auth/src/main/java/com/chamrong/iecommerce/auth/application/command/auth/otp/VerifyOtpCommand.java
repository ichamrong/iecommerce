package com.chamrong.iecommerce.auth.application.command.auth.otp;

import org.springframework.lang.NonNull;

/**
 * Command to verify a submitted one-time password.
 *
 * @param userId the authenticated user's local identifier
 * @param purpose must match the purpose used when sending the OTP
 * @param submittedCode the 6-digit code entered by the user
 */
public record VerifyOtpCommand(
    @NonNull String userId, @NonNull String purpose, @NonNull String submittedCode) {}
