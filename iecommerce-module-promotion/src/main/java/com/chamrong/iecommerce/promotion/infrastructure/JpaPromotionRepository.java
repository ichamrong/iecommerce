package com.chamrong.iecommerce.promotion.infrastructure;

import com.chamrong.iecommerce.promotion.domain.Promotion;
import com.chamrong.iecommerce.promotion.domain.PromotionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link PromotionRepository} port. */
@Repository
public interface JpaPromotionRepository
    extends JpaRepository<Promotion, Long>, PromotionRepository {}
