package com.chamrong.iecommerce.report.domain.ports;

import com.chamrong.iecommerce.report.domain.model.ExportJob;
import com.chamrong.iecommerce.report.domain.model.IdempotencyKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistence port for {@link ExportJob} aggregates. */
public interface ExportJobRepositoryPort {

  ExportJob save(ExportJob job);

  Optional<ExportJob> findById(UUID id);

  Optional<ExportJob> findByTenantAndIdempotencyKey(String tenantId, IdempotencyKey idempotencyKey);

  List<ExportJob> findPage(String tenantId, String status, int limitPlusOne, Object cursorPayload);
}
