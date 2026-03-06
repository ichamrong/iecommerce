package com.chamrong.iecommerce.booking.infrastructure.job;

import com.chamrong.iecommerce.booking.BookingAutoCancelledEvent;
import com.chamrong.iecommerce.booking.BookingSlaWarningEvent;
import com.chamrong.iecommerce.booking.domain.Booking;
import com.chamrong.iecommerce.booking.domain.BookingRepository;
import com.chamrong.iecommerce.booking.domain.BookingStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Monitors booking SLAs to ensure hosts respond in a timely manner. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "iecommerce.scheduler.sla-monitor",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SlaMonitorJob {

  private final BookingRepository bookingRepository;
  private final ApplicationEventPublisher eventPublisher;

  /** Runs every 15 minutes to find PENDING bookings nearing their SLA expiration. */
  @Scheduled(fixedDelay = 900000)
  public void monitorHostResponseSla() {
    log.info("Running Booking SLA Monitor Job");

    Instant warningThreshold = Instant.now().minus(20, ChronoUnit.HOURS);
    Instant breachThreshold = Instant.now().minus(24, ChronoUnit.HOURS);

    // 1. Fetch PENDING bookings created earlier than warningThreshold
    List<Booking> pendingBookings =
        bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, warningThreshold);

    for (Booking booking : pendingBookings) {
      if (booking.getCreatedAt().isBefore(breachThreshold)) {
        // SLA Breached (24 Hours passed)
        log.warn("Booking id={} breached 24h SLA. Auto-rejecting.", booking.getId());
        booking.reject("Auto-rejected due to SLA breach (Host failed to respond within 24 hours)");
        bookingRepository.save(booking);

        // Notify Customer of Cancellation
        eventPublisher.publishEvent(
            new BookingAutoCancelledEvent(
                booking.getTenantId(),
                booking.getId(),
                booking.getCustomerId(),
                booking.getGuestEmail()));

      } else {
        // Warning (20 Hours passed, 4 Hours left)
        log.info(
            "Booking id={} is nearing SLA limit. Dispatching multi-channel alert to host.",
            booking.getId());

        eventPublisher.publishEvent(
            new BookingSlaWarningEvent(
                booking.getTenantId(),
                booking.getId(),
                booking.getResourceProductId() // Assuming resourceProductId acts as identifying the
                // host's lodging constraint
                ));
      }
    }
  }
}
