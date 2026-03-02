package com.chamrong.iecommerce.review.application;

import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import com.chamrong.iecommerce.review.domain.Review;
import org.springframework.stereotype.Component;

/**
 * Maps review domain entities to API-facing DTOs.
 *
 * <p>Centralising the mapping keeps controllers and use case handlers focused on orchestration and
 * business rules.
 */
@Component
public class ReviewResponseMapper {

  /**
   * Convert a persisted {@link Review} aggregate into a {@link ReviewResponse} DTO for API
   * consumers.
   *
   * @param review the persisted review entity
   * @return DTO representation suitable for REST responses
   */
  public ReviewResponse toResponse(Review review) {
    return new ReviewResponse(
        review.getId(),
        review.getProductId(),
        review.getCustomerId(),
        review.getBookingId(),
        review.isAnonymous(),
        review.getRating(),
        review.getCleanlinessRating(),
        review.getAccuracyRating(),
        review.getCommunicationRating(),
        review.getLocationRating(),
        review.getCheckInRating(),
        review.getValueRating(),
        review.getComment(),
        review.getMediaKeys(),
        review.getStatus().name(),
        review.isFlaggedByOwner(),
        review.getFlagReason(),
        review.getOwnerReply(),
        review.getCreatedAt());
  }
}
