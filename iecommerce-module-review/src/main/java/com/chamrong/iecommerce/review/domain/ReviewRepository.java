package com.chamrong.iecommerce.review.domain;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
  Review save(Review review);

  List<Review> findByProductIdAndStatus(Long productId, String status);

  Optional<Review> findById(Long id);
}
