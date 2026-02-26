package com.chamrong.iecommerce.review.domain;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
  Review save(Review review);

  Optional<Review> findById(Long id);

  List<Review> findByProductIdAndStatus(Long productId, String status);

  List<Review> findByStatus(String status);
}
