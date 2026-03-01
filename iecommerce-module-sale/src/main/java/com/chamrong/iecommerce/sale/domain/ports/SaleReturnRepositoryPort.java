package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for sale return persistence. Keyset pagination: created_at DESC, id DESC. */
public interface SaleReturnRepositoryPort {

  SaleReturn save(SaleReturn saleReturn);

  Optional<SaleReturn> findByIdAndTenantId(Long id, String tenantId);

  Optional<SaleReturn> findByReturnKey(String tenantId, String returnKey);

  List<SaleReturn> findPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);
}
