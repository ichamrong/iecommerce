package com.chamrong.iecommerce.promotion.domain.rule.policy;

import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import java.math.BigDecimal;
import java.util.List;

/** Interface for selecting which promotions to apply when multiple are eligible. */
public interface SelectionPolicy {
  List<Candidate> select(List<Candidate> candidates);

  record Candidate(Promotion promotion, BigDecimal potentialDiscount) {}
}
