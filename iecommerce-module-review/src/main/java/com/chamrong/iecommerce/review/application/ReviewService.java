package com.chamrong.iecommerce.review.application;

import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

  private final ReviewRepository reviewRepository;

  public ReviewService(ReviewRepository reviewRepository) {
    this.reviewRepository = reviewRepository;
  }

  @Transactional
  public Review submitReview(Review review) {
    return reviewRepository.save(review);
  }

  public List<Review> getProductReviews(Long productId) {
    return reviewRepository.findByProductIdAndStatus(productId, "APPROVED");
  }

  @Transactional
  public void approveReview(Long reviewId) {
    reviewRepository
        .findById(reviewId)
        .ifPresent(
            review -> {
              review.setStatus("APPROVED");
              reviewRepository.save(review);
            });
  }
}
