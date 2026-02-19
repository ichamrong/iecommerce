package com.chamrong.iecommerce.promotion.infrastructure;

import com.chamrong.iecommerce.promotion.domain.Promotion;
import com.chamrong.iecommerce.promotion.domain.PromotionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPromotionRepository implements PromotionRepository {

  private final PromotionJpaInterface jpaInterface;

  public JpaPromotionRepository(PromotionJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Promotion save(Promotion promotion) {
    return jpaInterface.save(promotion);
  }

  @Override
  public Optional<Promotion> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public List<Promotion> findAll() {
    return jpaInterface.findAll();
  }

  public interface PromotionJpaInterface extends JpaRepository<Promotion, Long> {}
}
