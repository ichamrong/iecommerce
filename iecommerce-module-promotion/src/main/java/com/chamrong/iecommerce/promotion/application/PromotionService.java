package com.chamrong.iecommerce.promotion.application;

import com.chamrong.iecommerce.promotion.domain.Promotion;
import com.chamrong.iecommerce.promotion.domain.PromotionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {

  private final PromotionRepository promotionRepository;

  public PromotionService(PromotionRepository promotionRepository) {
    this.promotionRepository = promotionRepository;
  }

  @Transactional
  public Promotion createPromotion(Promotion promotion) {
    return promotionRepository.save(promotion);
  }

  public Optional<Promotion> getPromotionById(Long id) {
    return promotionRepository.findById(id);
  }

  public List<Promotion> getAllPromotions() {
    return promotionRepository.findAll();
  }
}
