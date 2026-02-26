package com.chamrong.iecommerce.review.infrastructure;

import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link ReviewRepository} port. */
@Repository
public interface JpaReviewRepository extends JpaRepository<Review, Long>, ReviewRepository {

  @Override
  List<Review> findByProductIdAndStatus(Long productId, String status);

  @Override
  List<Review> findByStatus(String status);
}
