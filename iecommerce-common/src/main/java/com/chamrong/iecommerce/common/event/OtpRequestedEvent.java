package com.chamrong.iecommerce.common.event;

/**
 * Published by the Auth module when an OTP needs to be dispatched to a user.
 *
 * <p>Decouples auth → notification: the Notification module listens for this event and sends the
 * actual email, so neither module depends on the other directly.
 *
 * @param userId internal user ID (for throttle / audit)
 * @param recipientEmail destination email address
 * @param purpose OTP purpose (e.g. "FORGOT_PASSWORD", "VERIFY_EMAIL")
 * @param otpCode the generated OTP code to include in the message
 */
public record OtpRequestedEvent(
    String userId, String recipientEmail, String purpose, String otpCode) {}
