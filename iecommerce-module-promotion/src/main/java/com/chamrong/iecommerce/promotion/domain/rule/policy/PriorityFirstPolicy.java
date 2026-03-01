package com.chamrong.iecommerce.promotion.domain.rule.policy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/** Policy that selects promotions based on their priority score. */
@Component
public class PriorityFirstPolicy implements SelectionPolicy {
  @Override
  public List<Candidate> select(List<Candidate> candidates) {
    List<Candidate> sorted = new ArrayList<>(candidates);
    sorted.sort(Comparator.comparing((Candidate c) -> c.promotion().getPriority()).reversed());

    // Similar stacking logic
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
