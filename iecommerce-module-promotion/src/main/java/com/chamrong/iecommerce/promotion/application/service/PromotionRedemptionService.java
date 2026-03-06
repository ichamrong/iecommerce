package com.chamrong.iecommerce.promotion.application.service;

import com.chamrong.iecommerce.promotion.application.dto.AppliedPromotionBreakdown;
import com.chamrong.iecommerce.promotion.application.port.ApplyPromotionUseCase;
import com.chamrong.iecommerce.promotion.domain.event.PromotionEventPublisher;
import com.chamrong.iecommerce.promotion.domain.exception.PromotionDomainException;
import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionRedemption;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRedemptionRepository;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRepository;
import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.engine.PromotionEngine;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class PromotionRedemptionService implements ApplyPromotionUseCase {

  private final PromotionRepository promotionRepository;
  private final PromotionRedemptionRepository redemptionRepository;
  private final PromotionEventPublisher eventPublisher;
  private final PromotionEngine promotionEngine;

  public PromotionRedemptionService(
      PromotionRepository promotionRepository,
      PromotionRedemptionRepository redemptionRepository,
      PromotionEventPublisher eventPublisher,
      PromotionEngine promotionEngine) {
    this.promotionRepository = promotionRepository;
    this.redemptionRepository = redemptionRepository;
    this.eventPublisher = eventPublisher;
    this.promotionEngine = promotionEngine;
  }

  @Override
  @Transactional
  public PromotionRedemption reserve(
      String tenantId,
      String code,
      String orderId,
      String customerId,
      String redemptionKey,
      PromotionContext context) {

    Optional<PromotionRedemption> existing =
        redemptionRepository.findByRedemptionKey(tenantId, redemptionKey);
    if (existing.isPresent()) {
      return existing.get();
    }

    Promotion promotion =
        promotionRepository
            .findByTenantIdAndCode(tenantId, code)
            .orElseThrow(() -> new PromotionDomainException("Invalid promotion code"));

    if (!promotion.isEligibleAt(
        context.getEvaluationTime() != null
            ? context.getEvaluationTime()
            : java.time.Instant.now())) {
      throw new PromotionDomainException("Promotion not active or expired");
    }

    // Use core engine to calculate discount for this specific code
    PromotionEngine.PricingResult pricingResult = promotionEngine.calculate(context);

    BigDecimal discountAmount =
        pricingResult.appliedPromotions().stream()
            .filter(p -> p.code().equalsIgnoreCase(code))
            .findFirst()
            .map(AppliedPromotionBreakdown::amount)
            .orElseThrow(
                () ->
                    new PromotionDomainException(
                        "Promotion criteria not met or superseded by policy"));

    PromotionRedemption redemption =
        PromotionRedemption.reserve(
            promotion, tenantId, orderId, customerId, redemptionKey, discountAmount);

    PromotionRedemption saved = redemptionRepository.save(redemption);
    eventPublisher.publish(tenantId, "PROMOTION_RESERVED", saved.getId(), saved);
    return saved;
  }

  @Override
  @Transactional
  public void apply(String tenantId, String redemptionKey) {
    PromotionRedemption redemption =
        redemptionRepository
            .findByRedemptionKey(tenantId, redemptionKey)
            .orElseThrow(() -> new PromotionDomainException("Redemption not found"));

    redemption.apply();
    redemptionRepository.save(redemption);

    Promotion promotion = redemption.getPromotion();
    promotion.recordRedemption();
    promotionRepository.save(promotion);

    eventPublisher.publish(tenantId, "PROMOTION_APPLIED", redemption.getId(), redemption);
  }

  @Override
  @Transactional
  public void release(String tenantId, String redemptionKey) {
    PromotionRedemption redemption =
        redemptionRepository
            .findByRedemptionKey(tenantId, redemptionKey)
            .orElseThrow(() -> new PromotionDomainException("Redemption not found"));

    redemption.release();
    redemptionRepository.save(redemption);
    eventPublisher.publish(tenantId, "PROMOTION_RELEASED", redemption.getId(), redemption);
  }
}
