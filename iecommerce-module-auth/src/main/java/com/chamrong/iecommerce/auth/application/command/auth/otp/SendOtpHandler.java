package com.chamrong.iecommerce.auth.application.command.auth.otp;

import com.chamrong.iecommerce.auth.infrastructure.otp.OtpStore;
import com.chamrong.iecommerce.common.event.OtpRequestedEvent;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Generates and dispatches a one-time password to the user's email.
 *
 * <h3>Rate abuse protection</h3>
 *
 * <p>If an active (unexpired, unused) code already exists for the same user+purpose, the handler
 * checks whether it is less than 60 seconds old. If so, it refuses to issue a new one — this
 * prevents email flooding from rapid re-sends.
 *
 * <h3>Async</h3>
 *
 * <p>Email dispatch runs on the {@code authTaskExecutor} thread pool so the HTTP response returns
 * immediately and the DB connection is not held open during the SMTP round-trip.
 *
 * <h3>Decoupling</h3>
 *
 * <p>Rather than calling {@code NotificationApi} directly (which would create a circular Maven
 * dependency: auth → notification → customer → auth), this handler publishes an {@link
 * OtpRequestedEvent}. The Notification module listens for this event and performs the actual email
 * send.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SendOtpHandler {

  /**
   * Minimum time between OTP sends for the same user+purpose. Prevents email flooding from rapid
   * re-sends.
   */
  private static final Duration MIN_RESEND_INTERVAL = Duration.ofSeconds(60);

  private final OtpStore otpStore;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Generates an OTP and dispatches it asynchronously via a Spring application event.
   *
   * @param command the send OTP command
   * @param recipientEmail the email address to send the OTP to
   */
  @Async("authTaskExecutor")
  public void handle(@NonNull final SendOtpCommand command, @NonNull final String recipientEmail) {
    // Throttle: refuse if a fresh code was sent less than MIN_RESEND_INTERVAL ago
    final Optional<com.chamrong.iecommerce.auth.infrastructure.otp.OtpEntry> existing =
        otpStore.find(command.userId(), command.purpose());

    if (existing.isPresent()
        && !existing.get().isExpired()
        && !existing.get().used()
        && java.time.Instant.now()
            .isBefore(
                existing
                    .get()
                    .expiresAt()
                    .minus(Duration.ofMinutes(5).minus(MIN_RESEND_INTERVAL)))) {
      log.warn(
          "OTP resend throttled for userId={} purpose={}", command.userId(), command.purpose());
      return;
    }

    final String code = otpStore.generate(command.userId(), command.purpose());

    log.info("OTP generated for userId={} purpose={}", command.userId(), command.purpose());

    // Publish event — Notification module picks this up and sends the email.
    // This avoids a direct auth → notification module dependency.
    eventPublisher.publishEvent(
        new OtpRequestedEvent(command.userId(), recipientEmail, command.purpose(), code));
  }
}
