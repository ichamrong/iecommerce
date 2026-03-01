package com.chamrong.iecommerce.promotion;

import com.chamrong.iecommerce.promotion.application.port.ApplyPromotionUseCase;
import com.chamrong.iecommerce.promotion.application.port.ValidatePromotionUseCase;
import com.chamrong.iecommerce.promotion.application.service.PromotionRedemptionService;
import com.chamrong.iecommerce.promotion.application.service.PromotionUseCaseService;
import com.chamrong.iecommerce.promotion.domain.event.PromotionEventPublisher;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRedemptionRepository;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRepository;
import com.chamrong.iecommerce.promotion.domain.rule.engine.PromotionEngine;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.chamrong.iecommerce.promotion")
@EntityScan(basePackages = "com.chamrong.iecommerce.promotion.domain.model")
@EnableJpaRepositories(
    basePackages = "com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa")
public class PromotionConfig {

  @Bean
  public ValidatePromotionUseCase validatePromotionUseCase(
      PromotionRepository promotionRepository,
      PromotionEventPublisher eventPublisher,
      PromotionEngine promotionEngine) {
    return new PromotionUseCaseService(promotionRepository, eventPublisher, promotionEngine);
  }

  @Bean
  public ApplyPromotionUseCase applyPromotionUseCase(
      PromotionRepository promotionRepository,
      PromotionRedemptionRepository redemptionRepository,
      PromotionEventPublisher eventPublisher,
      PromotionEngine promotionEngine) {
    return new PromotionRedemptionService(
        promotionRepository, redemptionRepository, eventPublisher, promotionEngine);
  }
}
