package com.chamrong.iecommerce.review.application.command;

import com.chamrong.iecommerce.common.event.ReviewApprovedEvent;
import com.chamrong.iecommerce.review.application.ReviewOutboxPublisher;
import com.chamrong.iecommerce.review.application.ReviewResponseMapper;
import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case handler for approving a review, making it visible on the storefront. */
@Service
@RequiredArgsConstructor
public class ApproveReviewCommandHandler {

  private static final Logger log = LoggerFactory.getLogger(ApproveReviewCommandHandler.class);

  private final ReviewRepository reviewRepository;
  private final ReviewResponseMapper mapper;
  private final ReviewOutboxPublisher outboxPublisher;

  /**
   * Approve the review with the given id.
   *
   * @param id review identifier
   * @return updated review DTO
   */
  @Transactional
  public ReviewResponse handle(Long id) {
    Review review =
        reviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
    review.approve();
    Review saved = reviewRepository.save(review);
    log.info("Approved review id={} tenantId={}", saved.getId(), saved.getTenantId());

    if (saved.getTenantId() != null) {
      outboxPublisher.publish(
          saved.getTenantId(),
          new ReviewApprovedEvent(
              saved.getTenantId(),
              saved.getId(),
              saved.getProductId(),
              saved.getCustomerId(),
              saved.getBookingId(),
              saved.getRating()));
    }

    return mapper.toResponse(saved);
  }
}
