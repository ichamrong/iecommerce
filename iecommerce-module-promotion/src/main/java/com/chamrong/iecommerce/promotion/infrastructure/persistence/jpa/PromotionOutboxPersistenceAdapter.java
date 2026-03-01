package com.chamrong.iecommerce.promotion.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.promotion.domain.model.PromotionOutboxEvent;
import com.chamrong.iecommerce.promotion.domain.port.PromotionOutboxPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionOutboxPersistenceAdapter implements PromotionOutboxPort {

  private final JpaPromotionOutboxRepository jpaPromotionOutboxRepository;

  @Override
  public PromotionOutboxEvent save(PromotionOutboxEvent event) {
    return jpaPromotionOutboxRepository.save(event);
  }

  @Override
  public List<PromotionOutboxEvent> findPending(int limit) {
    return jpaPromotionOutboxRepository.findPending(Instant.now(), PageRequest.of(0, limit));
  }
}
