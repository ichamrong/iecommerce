package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.domain.ports.ShiftRepositoryPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.ShiftEntity;
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
public class JpaShiftRepositoryAdapter implements ShiftRepositoryPort {

  private final SpringDataShiftRepository repository;
  private final SalePersistenceMapper mapper;

  @Override
  @Transactional
  public Shift save(Shift shift) {
    ShiftEntity entity = mapper.toEntity(shift);
    ShiftEntity saved = repository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Shift> findByIdAndTenantId(Long id, String tenantId) {
    return repository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
  }

  @Override
  public Optional<Shift> findActiveShift(String tenantId, String staffId, String terminalId) {
    return repository.findActiveShift(tenantId, staffId, terminalId).map(mapper::toDomain);
  }

  @Override
  public List<Shift> findPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    List<ShiftEntity> entities =
        repository.findPaged(tenantId, cursorId, cursorCreatedAt, PageRequest.of(0, limitPlusOne));
    return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
  }
}
