package com.chamrong.iecommerce.promotion.application.service;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.promotion.PromotionApi;
import com.chamrong.iecommerce.promotion.application.dto.PromotionRequest;
import com.chamrong.iecommerce.promotion.application.dto.PromotionResponse;
import com.chamrong.iecommerce.promotion.application.port.ValidatePromotionUseCase;
import com.chamrong.iecommerce.promotion.domain.event.PromotionEventPublisher;
import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import com.chamrong.iecommerce.promotion.domain.port.PromotionRepository;
import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.engine.PromotionEngine;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionUseCaseService implements ValidatePromotionUseCase, PromotionApi {

  private static final Logger log = LoggerFactory.getLogger(PromotionUseCaseService.class);

  private final PromotionRepository promotionRepository;
  private final PromotionEventPublisher eventPublisher;
  private final PromotionEngine promotionEngine;

  public PromotionUseCaseService(
      PromotionRepository promotionRepository,
      PromotionEventPublisher eventPublisher,
      PromotionEngine promotionEngine) {
    this.promotionRepository = promotionRepository;
    this.eventPublisher = eventPublisher;
    this.promotionEngine = promotionEngine;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Money> calculateDiscount(String tenantId, String code, Money baseAmount) {
    PromotionContext context =
        PromotionContext.builder().tenantId(tenantId).baseAmount(baseAmount).build();

    PromotionEngine.PricingResult result = promotionEngine.calculate(context);

    return result.appliedPromotions().stream()
        .filter(p -> p.code().equalsIgnoreCase(code))
        .findFirst()
        .map(p -> new Money(p.amount(), baseAmount.getCurrency()));
  }

  @Transactional
  public PromotionResponse create(String tenantId, PromotionRequest request) {
    Promotion promotion =
        Promotion.create(
            tenantId,
            request.name(),
            request.description(),
            request.type(),
            request.value(),
            request.code(),
            request.validFrom(),
            request.validTo(),
            request.priority(),
            request.isStackable(),
            request.usageLimit());
    Promotion saved = promotionRepository.save(promotion);
    eventPublisher.publish(tenantId, "PROMOTION_CREATED", saved.getId(), saved);
    return mapToResponse(saved);
  }

  @Transactional
  public PromotionResponse update(Long id, PromotionRequest request) {
    Promotion promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
    promotion.updateDetails(
        request.name(),
        request.description(),
        request.type(),
        request.value(),
        request.code(),
        request.validFrom(),
        request.validTo(),
        request.priority(),
        request.isStackable(),
        request.usageLimit());
    Promotion saved = promotionRepository.save(promotion);
    return mapToResponse(saved);
  }

  @Transactional
  public void delete(Long id) {
    promotionRepository.deleteById(id);
  }

  @Override
  public boolean validate(String tenantId, String code, PromotionContext context) {
    PromotionEngine.PricingResult result = promotionEngine.calculate(context);
    return result.appliedPromotions().stream().anyMatch(p -> p.code().equalsIgnoreCase(code));
  }

  public PromotionEngine.PricingResult calculate(PromotionContext context) {
    return promotionEngine.calculate(context);
  }

  public Optional<PromotionResponse> findById(Long id) {
    return promotionRepository.findById(id).map(this::mapToResponse);
  }

  public CursorPage<PromotionResponse> listPromotions(
      String tenantId, PromotionStatus status, Long lastId, int limit) {
    CursorPage<Promotion> page = promotionRepository.findAll(tenantId, status, lastId, limit);
    List<PromotionResponse> data =
        page.getData().stream().map(this::mapToResponse).collect(Collectors.toList());
    return new CursorPage<>(data, page.getNextCursor(), page.isHasMore());
  }

  public Optional<PromotionResponse> getPromotion(String tenantId, Long id) {
    return promotionRepository
        .findById(id)
        .filter(p -> p.getTenantId().equals(tenantId))
        .map(this::mapToResponse);
  }

  private PromotionResponse mapToResponse(Promotion p) {
    return new PromotionResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getType(),
        p.getValue(),
        p.getCode(),
        p.getValidFrom(),
        p.getValidTo(),
        p.getStatus(),
        p.getPriority(),
        p.isStackable(),
        p.getUsageLimit(),
        p.getUsedCount());
  }
}
