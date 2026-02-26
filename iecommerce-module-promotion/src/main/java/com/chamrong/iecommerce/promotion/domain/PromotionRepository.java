package com.chamrong.iecommerce.promotion.domain;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository {
  Promotion save(Promotion promotion);

  Optional<Promotion> findById(Long id);

  List<Promotion> findAll();

  void delete(Promotion promotion);

  List<Promotion> findByTenantId(String tenantId);

  Optional<Promotion> findByTenantIdAndCode(String tenantId, String code);
}
