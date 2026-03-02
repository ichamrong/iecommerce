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

/** Use case handler for owner replies to customer reviews. */
@Service
@RequiredArgsConstructor
public class ReplyToReviewCommandHandler {

  private static final Logger log = LoggerFactory.getLogger(ReplyToReviewCommandHandler.class);

  private final ReviewRepository reviewRepository;
  private final ReviewResponseMapper mapper;

  /**
   * Attach or update an owner reply on the given review.
   *
   * @param id review identifier
   * @param reply reply text
   * @return updated review DTO
   */
  @Transactional
  public ReviewResponse handle(Long id, String reply) {
    Review review =
        reviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
    review.replyAsOwner(reply);
    Review saved = reviewRepository.save(review);
    log.info("Owner replied to review id={}", saved.getId());
    return mapper.toResponse(saved);
  }
}
