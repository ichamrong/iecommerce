package com.chamrong.iecommerce.notification.infrastructure.event;

import com.chamrong.iecommerce.booking.BookingAutoCancelledEvent;
import com.chamrong.iecommerce.booking.BookingSlaWarningEvent;
import com.chamrong.iecommerce.notification.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingSlaNotificationListener {

  private final NotificationService notificationService;

  @EventListener
  public void onSlaWarning(BookingSlaWarningEvent event) {
    log.info("Received SLA Warning event for booking id={}", event.bookingId());
    
    // Using a stub ID for the host's push notification target based on property details 
    String hostTargetId = "host-" + event.hostId();
    notificationService.sendNotification(
        event.tenantId(),
        hostTargetId,
        "URGENT: Booking SLA Warning",
        "Booking #" + event.bookingId() + " will auto-cancel in 4 hours if not accepted immediately."
    );
  }

  @EventListener
  public void onAutoCancelled(BookingAutoCancelledEvent event) {
    log.info("Received Auto Cancelled event for booking id={}", event.bookingId());
    
    // Notify the guest that their money is refunded and the reservation was rejected by system SLA
    notificationService.sendNotification(
        event.tenantId(),
        event.guestEmail(),
        "Booking Auto-Cancelled: Host Unresponsive",
        "We're sorry, but the host did not confirm your booking (#" + event.bookingId() + ") within the requested time. You have been fully refunded."
    );
  }
}
