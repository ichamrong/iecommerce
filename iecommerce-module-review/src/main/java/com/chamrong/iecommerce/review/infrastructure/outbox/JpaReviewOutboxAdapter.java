package com.chamrong.iecommerce.review.infrastructure.outbox;

import com.chamrong.iecommerce.review.domain.ReviewOutboxEvent;
import com.chamrong.iecommerce.review.domain.ports.ReviewOutboxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing {@link ReviewOutboxPort} by persisting events to the review outbox table.
 */
@Component
@RequiredArgsConstructor
public class JpaReviewOutboxAdapter implements ReviewOutboxPort {

  private final SpringDataReviewOutboxRepository outboxRepository;

  @Override
  public void save(String tenantId, String eventType, String payload) {
    outboxRepository.save(ReviewOutboxEvent.pending(tenantId, eventType, payload));
  }
}
