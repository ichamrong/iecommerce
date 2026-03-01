package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import java.util.Optional;

public interface ShiftRepositoryPort {

  Shift save(Shift shift);

  Optional<Shift> findByIdAndTenantId(Long id, String tenantId);

  Optional<Shift> findActiveShift(String tenantId, String staffId, String terminalId);

  CursorPage<Shift> findAll(String tenantId, String cursor, int limit);
}
