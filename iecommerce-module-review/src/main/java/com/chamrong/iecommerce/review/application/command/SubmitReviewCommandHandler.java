package com.chamrong.iecommerce.review.application.command;

import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.ReviewSubmittedEvent;
import com.chamrong.iecommerce.review.application.ReviewOutboxPublisher;
import com.chamrong.iecommerce.review.application.ReviewResponseMapper;
import com.chamrong.iecommerce.review.application.dto.ReviewRequest;
import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import com.chamrong.iecommerce.review.domain.ReviewStatus;
import com.chamrong.iecommerce.review.domain.model.Rating;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case handler for submitting a new product review.
 *
 * <p>Validates input, constructs the {@link Review} aggregate, and persists it using the configured
 * repository.
 */
@Service
@RequiredArgsConstructor
public class SubmitReviewCommandHandler {

  private static final Logger log = LoggerFactory.getLogger(SubmitReviewCommandHandler.class);

  private final ReviewRepository reviewRepository;
  private final ReviewResponseMapper mapper;
  private final ReviewOutboxPublisher outboxPublisher;

  /**
   * Submit a new review for moderation.
   *
   * @param req API request payload
   * @return created review as DTO
   */
  @Transactional
  public ReviewResponse handle(ReviewRequest req) {
    Objects.requireNonNull(req, "ReviewRequest must not be null");

    Integer rawRating = req.rating();
    if (rawRating == null) {
      throw new IllegalArgumentException("Rating must not be null");
    }
    Rating overallRating = Rating.of(rawRating);

    String tenantId = TenantContext.requireTenantId();

    Review review = new Review();
    review.setProductId(req.productId());
    review.setCustomerId(req.customerId());
    review.setBookingId(req.bookingId());
    review.setAnonymous(req.isAnonymous());
    review.setRating(overallRating.getValue());
    if (req.cleanlinessRating() != null) {
      review.setCleanlinessRating(Rating.of(req.cleanlinessRating()).getValue());
    }
    if (req.accuracyRating() != null) {
      review.setAccuracyRating(Rating.of(req.accuracyRating()).getValue());
    }
    if (req.communicationRating() != null) {
      review.setCommunicationRating(Rating.of(req.communicationRating()).getValue());
    }
    if (req.locationRating() != null) {
      review.setLocationRating(Rating.of(req.locationRating()).getValue());
    }
    if (req.checkInRating() != null) {
      review.setCheckInRating(Rating.of(req.checkInRating()).getValue());
    }
    if (req.valueRating() != null) {
      review.setValueRating(Rating.of(req.valueRating()).getValue());
    }
    review.setTenantId(tenantId);
    review.setComment(req.comment());
    review.setMediaKeys(req.mediaKeys());
    review.setStatus(ReviewStatus.PENDING);

    Review saved = reviewRepository.save(review);
    log.info(
        "Submitted review id={} for productId={} tenantId={}",
        saved.getId(),
        saved.getProductId(),
        tenantId);

    outboxPublisher.publish(
        tenantId,
        new ReviewSubmittedEvent(
            tenantId,
            saved.getId(),
            saved.getProductId(),
            saved.getCustomerId(),
            saved.getBookingId(),
            saved.getRating()));

    return mapper.toResponse(saved);
  }
}
