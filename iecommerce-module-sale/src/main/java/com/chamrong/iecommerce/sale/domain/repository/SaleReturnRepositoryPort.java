package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import java.util.Optional;

public interface SaleReturnRepositoryPort {

  SaleReturn save(SaleReturn saleReturn);

  Optional<SaleReturn> findByIdAndTenantId(Long id, String tenantId);

  Optional<SaleReturn> findByReturnKey(String tenantId, String returnKey);

  CursorPage<SaleReturn> findAll(String tenantId, String cursor, int limit);
}
