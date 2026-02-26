package com.chamrong.iecommerce.review.application;

import com.chamrong.iecommerce.review.application.dto.ReviewRequest;
import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import com.chamrong.iecommerce.review.domain.ReviewStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;

  @Transactional
  public ReviewResponse submit(ReviewRequest req) {
    if (req.rating() < 1 || req.rating() > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }
    Review review = new Review();
    review.setProductId(req.productId());
    review.setCustomerId(req.customerId());
    review.setBookingId(req.bookingId());
    review.setAnonymous(req.isAnonymous());
    review.setRating(req.rating());
    review.setCleanlinessRating(req.cleanlinessRating());
    review.setAccuracyRating(req.accuracyRating());
    review.setCommunicationRating(req.communicationRating());
    review.setLocationRating(req.locationRating());
    review.setCheckInRating(req.checkInRating());
    review.setValueRating(req.valueRating());
    review.setComment(req.comment());
    review.setMediaKeys(req.mediaKeys());
    review.setStatus(ReviewStatus.PENDING);
    return toResponse(reviewRepository.save(review));
  }

  @Transactional(readOnly = true)
  public List<ReviewResponse> getApprovedReviews(Long productId) {
    return reviewRepository
        .findByProductIdAndStatus(productId, ReviewStatus.APPROVED.name())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ReviewResponse> getPendingReviews() {
    return reviewRepository
        .findByStatus(ReviewStatus.PENDING.name())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public ReviewResponse approve(Long id) {
    Review review = requireReview(id);
    review.approve();
    return toResponse(reviewRepository.save(review));
  }

  @Transactional
  public ReviewResponse reject(Long id) {
    Review review = requireReview(id);
    review.reject();
    return toResponse(reviewRepository.save(review));
  }

  @Transactional
  public ReviewResponse flagReview(Long id, String reason) {
    Review review = requireReview(id);
    review.setStatus(ReviewStatus.PENDING);
    review.setFlaggedByOwner(true);
    review.setFlagReason(reason);
    return toResponse(reviewRepository.save(review));
  }

  @Transactional
  public ReviewResponse replyToReview(Long id, String reply) {
    Review review = requireReview(id);
    review.setOwnerReply(reply);
    return toResponse(reviewRepository.save(review));
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private Review requireReview(Long id) {
    return reviewRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
  }

  private ReviewResponse toResponse(Review r) {
    return new ReviewResponse(
        r.getId(),
        r.getProductId(),
        r.getCustomerId(),
        r.getBookingId(),
        r.isAnonymous(),
        r.getRating(),
        r.getCleanlinessRating(),
        r.getAccuracyRating(),
        r.getCommunicationRating(),
        r.getLocationRating(),
        r.getCheckInRating(),
        r.getValueRating(),
        r.getComment(),
        r.getMediaKeys(),
        r.getStatus().name(),
        r.isFlaggedByOwner(),
        r.getFlagReason(),
        r.getOwnerReply(),
        r.getCreatedAt());
  }
}
