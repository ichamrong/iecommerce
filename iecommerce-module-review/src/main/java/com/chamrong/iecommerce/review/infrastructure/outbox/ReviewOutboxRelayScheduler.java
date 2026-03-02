package com.chamrong.iecommerce.review.infrastructure.outbox;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.event.ReviewApprovedEvent;
import com.chamrong.iecommerce.common.event.ReviewRejectedEvent;
import com.chamrong.iecommerce.common.event.ReviewSubmittedEvent;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.chamrong.iecommerce.review.domain.ReviewOutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically relays review outbox events to the {@link EventDispatcher}.
 *
 * <p>Follows the same pattern as payment and order modules to ensure reliable cross-module
 * messaging.
 */
@Component
public class ReviewOutboxRelayScheduler extends AbstractOutboxRelay<ReviewOutboxEvent> {

  private static final Logger log = LoggerFactory.getLogger(ReviewOutboxRelayScheduler.class);

  private final SpringDataReviewOutboxRepository outboxRepository;

  public ReviewOutboxRelayScheduler(
      SpringDataReviewOutboxRepository outboxRepository,
      EventDispatcher eventDispatcher,
      ObjectMapper objectMapper) {
    super(eventDispatcher, objectMapper);
    this.outboxRepository = outboxRepository;
  }

  @Scheduled(fixedDelay = 5000)
  @org.springframework.transaction.annotation.Transactional
  public void relay() {
    try {
      processPendingEvents(
          outboxRepository.findPendingForUpdate(
              org.springframework.data.domain.PageRequest.of(0, 50)));
    } catch (Exception e) {
      log.error("Review outbox relay failed", e);
      throw e;
    }
  }

  @Override
  protected void saveEvent(ReviewOutboxEvent event) {
    outboxRepository.save(event);
  }

  @Override
  protected Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      case "ReviewSubmittedEvent" -> ReviewSubmittedEvent.class;
      case "ReviewApprovedEvent" -> ReviewApprovedEvent.class;
      case "ReviewRejectedEvent" -> ReviewRejectedEvent.class;
      default -> Object.class;
    };
  }
}
