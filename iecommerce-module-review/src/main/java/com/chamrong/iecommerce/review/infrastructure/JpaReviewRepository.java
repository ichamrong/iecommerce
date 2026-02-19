package com.chamrong.iecommerce.review.infrastructure;

import com.chamrong.iecommerce.review.domain.Review;
import com.chamrong.iecommerce.review.domain.ReviewRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaReviewRepository implements ReviewRepository {

  private final ReviewJpaInterface jpaInterface;

  public JpaReviewRepository(ReviewJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Review save(Review review) {
    return jpaInterface.save(review);
  }

  @Override
  public List<Review> findByProductIdAndStatus(Long productId, String status) {
    return jpaInterface.findByProductIdAndStatus(productId, status);
  }

  @Override
  public Optional<Review> findById(Long id) {
    return jpaInterface.findById(id);
  }

  public interface ReviewJpaInterface extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndStatus(Long productId, String status);
  }
}
