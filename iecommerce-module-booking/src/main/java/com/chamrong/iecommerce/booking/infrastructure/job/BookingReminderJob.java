package com.chamrong.iecommerce.booking.infrastructure.job;

import com.chamrong.iecommerce.booking.BookingReminderEvent;
import com.chamrong.iecommerce.booking.domain.Booking;
import com.chamrong.iecommerce.booking.domain.BookingRepository;
import com.chamrong.iecommerce.booking.domain.BookingStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingReminderJob {

  private final BookingRepository bookingRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Runs every hour to find bookings starting in approximately 24 hours. To avoid duplicate
   * reminders, in a real system we'd mark them as 'reminder_sent'.
   */
  @Scheduled(cron = "0 0 * * * *")
  public void sendReminders() {
    Instant now = Instant.now();
    Instant windowStart = now.plus(23, ChronoUnit.HOURS);
    Instant windowEnd = now.plus(25, ChronoUnit.HOURS);

    log.info("Running booking reminder job for window [{} - {}]", windowStart, windowEnd);

    List<Booking> upcoming =
        bookingRepository.findByStatusAndStartAtBetween(
            BookingStatus.ACCEPTED, windowStart, windowEnd);

    for (Booking booking : upcoming) {
      // Logic to check if reminder already sent would go here
      eventPublisher.publishEvent(
          new BookingReminderEvent(
              booking.getTenantId(),
              booking.getId(),
              booking.getCustomerId(),
              booking.getStartAt()));
    }

    log.info("Published {} booking reminder events", upcoming.size());
  }
}
