package com.chamrong.iecommerce.promotion.domain.rule.engine;

import com.chamrong.iecommerce.promotion.application.dto.AppliedPromotionBreakdown;
import com.chamrong.iecommerce.promotion.application.service.AllocationService;
import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.port.PromotionRepository;
import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.dsl.RuleDefinition;
import com.chamrong.iecommerce.promotion.domain.rule.dsl.RuleParser;
import com.chamrong.iecommerce.promotion.domain.rule.evaluator.ActionEvaluator;
import com.chamrong.iecommerce.promotion.domain.rule.evaluator.ConditionEvaluator;
import com.chamrong.iecommerce.promotion.domain.rule.evaluator.RuleRegistry;
import com.chamrong.iecommerce.promotion.domain.rule.policy.SelectionPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Core engine for evaluating and applying promotion rules. */
@Service
public class PromotionEngine {

  private static final Logger log = LoggerFactory.getLogger(PromotionEngine.class);

  private final PromotionRepository promotionRepository;
  private final RuleRegistry ruleRegistry;
  private final RuleParser ruleParser;
  private final AllocationService allocationService;
  private final SelectionPolicy selectionPolicy;

  public PromotionEngine(
      PromotionRepository promotionRepository,
      RuleRegistry ruleRegistry,
      RuleParser ruleParser,
      AllocationService allocationService,
      SelectionPolicy selectionPolicy) {
    this.promotionRepository = promotionRepository;
    this.ruleRegistry = ruleRegistry;
    this.ruleParser = ruleParser;
    this.allocationService = allocationService;
    this.selectionPolicy = selectionPolicy;
  }

  public PricingResult calculate(PromotionContext context) {
    // 1. Collect active candidates
    List<Promotion> candidates =
        promotionRepository.findAllActive(context.getTenantId(), context.getEvaluationTime());

    List<SelectionPolicy.Candidate> eligibleCandidates = new ArrayList<>();

    for (Promotion promo : candidates) {
      RuleDefinition rule = ruleParser.parse(promo.getRuleJson());
      if (rule == null) continue;

      // 2. Evaluate Eligibility
      if (evaluateCondition(rule.getCondition(), context)) {
        // 3. Compute Potential Discount
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (RuleDefinition.Action action : rule.getActions()) {
          totalDiscount = totalDiscount.add(computeAction(action, context));
        }

        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
          eligibleCandidates.add(new SelectionPolicy.Candidate(promo, totalDiscount));
        }
      }
    }

    // 4. Selection (Conflict Resolution)
    List<SelectionPolicy.Candidate> selected = selectionPolicy.select(eligibleCandidates);

    // 5. Finalize & Allocate
    BigDecimal totalDiscount =
        selected.stream()
            .map(SelectionPolicy.Candidate::potentialDiscount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Simple allocation of total across all selected
    List<AppliedPromotionBreakdown> applied =
        selected.stream()
            .map(
                c -> {
                  Map<String, BigDecimal> allocations =
                      allocationService.allocate(c.potentialDiscount(), context.getItems());
                  return new AppliedPromotionBreakdown(
                      c.promotion().getId(),
                      c.promotion().getCode(),
                      c.potentialDiscount(),
                      "Eligible and selected by policy",
                      allocations);
                })
            .collect(Collectors.toList());

    BigDecimal baseAmount =
        context.getBaseAmount() != null ? context.getBaseAmount().getAmount() : BigDecimal.ZERO;
    return new PricingResult(
        baseAmount,
        totalDiscount,
        baseAmount.subtract(totalDiscount).max(BigDecimal.ZERO),
        applied);
  }

  private boolean evaluateCondition(RuleDefinition.Condition condition, PromotionContext context) {
    if (condition == null) return true; // No condition means always eligible
    ConditionEvaluator evaluator = ruleRegistry.getConditionEvaluator(condition.getClass());
    return evaluator != null && evaluator.evaluate(condition, context);
  }

  private BigDecimal computeAction(RuleDefinition.Action action, PromotionContext context) {
    ActionEvaluator evaluator = ruleRegistry.getActionEvaluator(action.getClass());
    return evaluator != null ? evaluator.compute(action, context) : BigDecimal.ZERO;
  }

  public record PricingResult(
      BigDecimal totalBeforeDiscount,
      BigDecimal totalDiscount,
      BigDecimal totalAfterDiscount,
      List<AppliedPromotionBreakdown> appliedPromotions) {}
}
