package com.chamrong.iecommerce.notification.infrastructure.event;

import com.chamrong.iecommerce.common.event.OtpRequestedEvent;
import com.chamrong.iecommerce.notification.NotificationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for {@link OtpRequestedEvent} published by the Auth module and dispatches the OTP email
 * via {@link NotificationApi}.
 *
 * <p>This listener is the sole bridge between the Auth and Notification modules — neither module
 * imports the other at the Maven level. Auth publishes a shared-event record (defined in
 * iecommerce-common) and this listener reacts to it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtpEventListener {

  private final NotificationApi notificationApi;

  @Async
  @EventListener
  public void onOtpRequested(OtpRequestedEvent event) {
    log.info("Dispatching OTP email for userId={} purpose={}", event.userId(), event.purpose());

    notificationApi.sendNotification(
        "system",
        event.recipientEmail(),
        "Your verification code",
        String.format(
            "Your verification code is: %s%n%nThis code expires in 5 minutes. "
                + "Do not share it with anyone.",
            event.otpCode()));
  }
}
