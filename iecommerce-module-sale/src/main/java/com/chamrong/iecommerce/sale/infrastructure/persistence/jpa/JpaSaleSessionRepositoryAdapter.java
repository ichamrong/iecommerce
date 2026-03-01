package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import com.chamrong.iecommerce.sale.domain.ports.SaleSessionRepositoryPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleSessionEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.mapper.SalePersistenceMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JpaSaleSessionRepositoryAdapter implements SaleSessionRepositoryPort {

  private final SpringDataSaleSessionRepository repository;
  private final SalePersistenceMapper mapper;

  @Override
  @Transactional
  public SaleSession save(SaleSession session) {
    SaleSessionEntity entity = mapper.toEntity(session);
    SaleSessionEntity saved = repository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<SaleSession> findByIdAndTenantId(Long id, String tenantId) {
    return repository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
  }

  @Override
  public Optional<SaleSession> findActiveSessionByTerminal(String tenantId, String terminalId) {
    return repository.findActiveSession(tenantId, terminalId).map(mapper::toDomain);
  }

  @Override
  public List<SaleSession> findPage(
      String tenantId,
      String terminalId,
      Instant cursorCreatedAt,
      Long cursorId,
      int limitPlusOne) {
    List<SaleSessionEntity> entities =
        repository.findPaged(
            tenantId, terminalId, cursorId, cursorCreatedAt, PageRequest.of(0, limitPlusOne));
    return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
  }
}
