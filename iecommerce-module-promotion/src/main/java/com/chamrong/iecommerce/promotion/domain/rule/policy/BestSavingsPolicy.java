package com.chamrong.iecommerce.promotion.domain.rule.policy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/** Policy that selects the promotions that result in the highest total discount. */
@Component
public class BestSavingsPolicy implements SelectionPolicy {
  @Override
  public List<Candidate> select(List<Candidate> candidates) {
    // Sort by discount descending
    List<Candidate> sorted = new ArrayList<>(candidates);
    sorted.sort(Comparator.comparing(Candidate::potentialDiscount).reversed());

    List<Candidate> selected = new ArrayList<>();
    boolean exclusiveApplied = false;

    for (Candidate c : sorted) {
      if (exclusiveApplied) break;

      if (!c.promotion().isStackable()) {
        if (selected.isEmpty()) {
          selected.add(c);
          exclusiveApplied = true;
        }
      } else {
        selected.add(c);
      }
    }
    return selected;
  }
}
