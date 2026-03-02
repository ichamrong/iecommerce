package com.chamrong.iecommerce.review.application.query;

import com.chamrong.iecommerce.review.application.ReviewResponseMapper;
import com.chamrong.iecommerce.review.application.dto.ReviewResponse;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import com.chamrong.iecommerce.review.domain.ReviewStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Query handler for retrieving reviews that are awaiting moderation. */
@Service
@RequiredArgsConstructor
public class GetPendingReviewsQueryHandler {

  private static final Logger log = LoggerFactory.getLogger(GetPendingReviewsQueryHandler.class);

  private final ReviewRepository reviewRepository;
  private final ReviewResponseMapper mapper;

  /**
   * Return all reviews currently marked as {@link ReviewStatus#PENDING}.
   *
   * @return list of pending reviews
   */
  @Transactional(readOnly = true)
  public List<ReviewResponse> handle() {
    log.debug("Fetching pending reviews for moderation queue");
    return reviewRepository.findByStatus(ReviewStatus.PENDING.name()).stream()
        .map(mapper::toResponse)
        .toList();
  }
}
