package com.chamrong.iecommerce.promotion.infrastructure;

import com.chamrong.iecommerce.promotion.domain.Promotion;
import com.chamrong.iecommerce.promotion.domain.PromotionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link PromotionRepository} port. */
@Repository
public interface JpaPromotionRepository
    extends JpaRepository<Promotion, Long>, PromotionRepository {

  @Override
  List<Promotion> findByTenantId(String tenantId);

  @Override
  Optional<Promotion> findByTenantIdAndCode(String tenantId, String code);
}
