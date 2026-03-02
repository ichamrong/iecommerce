package com.chamrong.iecommerce.review.application.command;

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

/** Use case handler for flagging a review by the listing or store owner. */
@Service
@RequiredArgsConstructor
public class FlagReviewCommandHandler {

  private static final Logger log = LoggerFactory.getLogger(FlagReviewCommandHandler.class);

  private final ReviewRepository reviewRepository;
  private final ReviewResponseMapper mapper;

  /**
   * Flag a review for moderator attention.
   *
   * @param id review identifier
   * @param reason human-readable reason for the flag
   * @return updated review DTO
   */
  @Transactional
  public ReviewResponse handle(Long id, String reason) {
    Review review =
        reviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
    review.flagByOwner(reason);
    Review saved = reviewRepository.save(review);
    log.info("Flagged review id={} with reason={}", saved.getId(), saved.getFlagReason());
    return mapper.toResponse(saved);
  }
}
