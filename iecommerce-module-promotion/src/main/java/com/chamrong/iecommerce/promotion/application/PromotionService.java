package com.chamrong.iecommerce.promotion.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.promotion.PromotionApi;
import com.chamrong.iecommerce.promotion.application.dto.PromotionRequest;
import com.chamrong.iecommerce.promotion.application.dto.PromotionResponse;
import com.chamrong.iecommerce.promotion.domain.Promotion;
import com.chamrong.iecommerce.promotion.domain.PromotionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionService implements PromotionApi {

  private final PromotionRepository promotionRepository;
  private final com.chamrong.iecommerce.promotion.application.rule.PromotionRuleChecker ruleChecker;

  @Override
  @Transactional(readOnly = true)
  public Optional<Money> calculateDiscount(String tenantId, String code, Money baseAmount) {
    return promotionRepository
        .findByTenantIdAndCode(tenantId, code)
        .filter(p -> p.isActiveAt(Instant.now()))
        .map(p -> new Money(p.calculateDiscount(baseAmount.getAmount()), baseAmount.getCurrency()));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Money> calculateDiscount(
      String code, com.chamrong.iecommerce.promotion.application.rule.PromotionContext context) {
    return promotionRepository
        .findByTenantIdAndCode(context.getTenantId(), code)
        .filter(
            p ->
                p.isActiveAt(
                    context.getEvaluationTime() != null
                        ? context.getEvaluationTime()
                        : Instant.now()))
        .filter(p -> p.getRules().stream().allMatch(rule -> ruleChecker.isEligible(rule, context)))
        .map(
            p ->
                new Money(
                    p.calculateDiscount(context.getBaseAmount().getAmount()),
                    context.getBaseAmount().getCurrency()));
  }

  @Transactional(readOnly = true)
  public List<PromotionResponse> listAll(String tenantId) {
    return promotionRepository.findByTenantId(tenantId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public Optional<PromotionResponse> findById(Long id) {
    return promotionRepository.findById(id).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public Optional<PromotionResponse> findActiveByCode(String tenantId, String code) {
    return promotionRepository
        .findByTenantIdAndCode(tenantId, code)
        .filter(p -> p.isActiveAt(Instant.now()))
        .map(this::toResponse);
  }

  @Transactional
  public PromotionResponse create(String tenantId, PromotionRequest req) {
    Promotion p = new Promotion();
    p.setTenantId(tenantId);
    applyFields(p, req);
    return toResponse(promotionRepository.save(p));
  }

  @Transactional
  public PromotionResponse update(Long id, PromotionRequest req) {
    Promotion p =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promotion not found: " + id));
    applyFields(p, req);
    return toResponse(promotionRepository.save(p));
  }

  @Transactional
  public void delete(Long id) {
    Promotion p =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Promotion not found: " + id));
    promotionRepository.delete(p);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private void applyFields(Promotion p, PromotionRequest req) {
    if (req.name() != null) p.setName(req.name());
    if (req.description() != null) p.setDescription(req.description());
    if (req.type() != null) p.setType(req.type());
    if (req.value() != null) p.setValue(req.value());
    if (req.code() != null) p.setCode(req.code());
    if (req.validFrom() != null) p.setValidFrom(req.validFrom());
    if (req.validTo() != null) p.setValidTo(req.validTo());
    if (req.active() != null) p.setActive(req.active());
  }

  private PromotionResponse toResponse(Promotion p) {
    return new PromotionResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getType(),
        p.getValue(),
        p.getCode(),
        p.getValidFrom(),
        p.getValidTo(),
        p.isActive(),
        p.getCreatedAt());
  }
}
