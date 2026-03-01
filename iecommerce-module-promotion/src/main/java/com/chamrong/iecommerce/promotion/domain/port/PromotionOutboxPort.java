package com.chamrong.iecommerce.promotion.domain.port;

import com.chamrong.iecommerce.promotion.domain.model.PromotionOutboxEvent;
import java.util.List;

/** Port for managing outbox events. */
public interface PromotionOutboxPort {
  PromotionOutboxEvent save(PromotionOutboxEvent event);

  List<PromotionOutboxEvent> findPending(int limit);
}
