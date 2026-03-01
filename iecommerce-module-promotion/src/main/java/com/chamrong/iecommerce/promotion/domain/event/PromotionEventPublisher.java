package com.chamrong.iecommerce.promotion.domain.event;

public interface PromotionEventPublisher {
  void publish(String tenantId, String eventType, Long promotionId, Object payload);
}
