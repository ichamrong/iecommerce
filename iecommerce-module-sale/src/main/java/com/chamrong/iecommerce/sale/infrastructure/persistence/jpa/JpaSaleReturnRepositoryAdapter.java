package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import com.chamrong.iecommerce.sale.domain.ports.SaleReturnRepositoryPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleReturnEntity;
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
public class JpaSaleReturnRepositoryAdapter implements SaleReturnRepositoryPort {

  private final SpringDataSaleReturnRepository repository;
  private final SalePersistenceMapper mapper;

  @Override
  @Transactional
  public SaleReturn save(SaleReturn saleReturn) {
    SaleReturnEntity entity = mapper.toEntity(saleReturn);
    SaleReturnEntity saved = repository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<SaleReturn> findByIdAndTenantId(Long id, String tenantId) {
    return repository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
  }

  @Override
  public Optional<SaleReturn> findByReturnKey(String tenantId, String returnKey) {
    return repository.findByTenantIdAndReturnKey(tenantId, returnKey).map(mapper::toDomain);
  }

  @Override
  public List<SaleReturn> findPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    List<SaleReturnEntity> entities =
        repository.findPaged(tenantId, cursorId, cursorCreatedAt, PageRequest.of(0, limitPlusOne));
    return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
  }
}
