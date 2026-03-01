package com.chamrong.iecommerce.promotion.domain.rule.evaluator;

import com.chamrong.iecommerce.promotion.domain.rule.dsl.RuleDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for pluggable evaluators. */
@Component
public class RuleRegistry {
  private final Map<Class<? extends RuleDefinition.Condition>, ConditionEvaluator<?>>
      conditionEvaluators = new HashMap<>();
  private final Map<Class<? extends RuleDefinition.Action>, ActionEvaluator<?>> actionEvaluators =
      new HashMap<>();

  public RuleRegistry(List<ConditionEvaluator<?>> conditions, List<ActionEvaluator<?>> actions) {
    conditions.forEach(c -> conditionEvaluators.put(c.getConditionType(), c));
    actions.forEach(a -> actionEvaluators.put(a.getActionType(), a));
  }

  @SuppressWarnings("unchecked")
  public <T extends RuleDefinition.Condition> ConditionEvaluator<T> getConditionEvaluator(
      Class<T> type) {
    return (ConditionEvaluator<T>) conditionEvaluators.get(type);
  }

  @SuppressWarnings("unchecked")
  public <T extends RuleDefinition.Action> ActionEvaluator<T> getActionEvaluator(Class<T> type) {
    return (ActionEvaluator<T>) actionEvaluators.get(type);
  }
}
