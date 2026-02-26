package com.chamrong.iecommerce.notification.infrastructure.event;

import com.chamrong.iecommerce.customer.CustomerApi;
import com.chamrong.iecommerce.notification.NotificationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventListener {

  private final CustomerApi customerApi;
  private final NotificationApi notificationApi;

  @EventListener
  public void onBookingConfirmed(com.chamrong.iecommerce.booking.BookingConfirmedEvent event) {
    log.info("Handling booking confirmation for bookingId={}", event.bookingId());

    customerApi
        .getCustomer(event.customerId())
        .ifPresent(
            customer -> {
              String subject = "Booking Confirmed: #" + event.bookingId();
              String content =
                  "Your booking for resource "
                      + event.resourceProductId()
                      + " is confirmed.\n"
                      + "Time: "
                      + event.startAt()
                      + " to "
                      + event.endAt();

              notificationApi.sendNotification(
                  event.tenantId(), customer.email(), subject, content);
            });
  }

  @EventListener
  public void onBookingReminder(com.chamrong.iecommerce.booking.BookingReminderEvent event) {
    log.info(
        "Handling booking reminder for bookingId={} customerId={}",
        event.bookingId(),
        event.customerId());

    customerApi
        .getCustomer(event.customerId())
        .ifPresent(
            customer -> {
              String subject = "Reminder: Your booking #" + event.bookingId() + " is tomorrow!";
              String content =
                  "Hi "
                      + customer.firstName()
                      + ",\n\n"
                      + "This is a friendly reminder that your booking is scheduled for "
                      + event.startAt()
                      + ".\n"
                      + "We look forward to seeing you!";

              notificationApi.sendNotification(
                  event.tenantId(), customer.email(), subject, content);
            });
  }

  @EventListener
  public void onBookingCompleted(com.chamrong.iecommerce.booking.BookingCompletedEvent event) {
    log.info("Handling booking completion for bookingId={}", event.bookingId());

    customerApi
        .getCustomer(event.customerId())
        .ifPresent(
            customer -> {
              String subject = "How was your experience? Booking #" + event.bookingId();
              String content =
                  "Please leave a review for your recent booking: " + event.resourceProductId();

              notificationApi.sendNotification(
                  event.tenantId(), customer.email(), subject, content);
            });
  }
}
