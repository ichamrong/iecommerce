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

/**
 * Query handler for fetching approved reviews for a given product.
 *
 * <p>Encapsulates the read-model logic used by storefront product detail pages.
 */
@Service
@RequiredArgsConstructor
public class GetApprovedReviewsQueryHandler {

  private static final Logger log = LoggerFactory.getLogger(GetApprovedReviewsQueryHandler.class);

  private final ReviewRepository reviewRepository;
  private final ReviewResponseMapper mapper;

  /**
   * Fetch all approved reviews for a product.
   *
   * @param productId product identifier
   * @return list of approved reviews
   */
  @Transactional(readOnly = true)
  public List<ReviewResponse> handle(Long productId) {
    log.debug("Fetching approved reviews for productId={}", productId);
    return reviewRepository
        .findByProductIdAndStatus(productId, ReviewStatus.APPROVED.name())
        .stream()
        .map(mapper::toResponse)
        .toList();
  }
}
