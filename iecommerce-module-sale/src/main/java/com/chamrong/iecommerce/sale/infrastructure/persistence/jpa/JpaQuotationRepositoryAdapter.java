package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.sale.domain.model.Quotation;
import com.chamrong.iecommerce.sale.domain.ports.QuotationRepositoryPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.QuotationEntity;
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
public class JpaQuotationRepositoryAdapter implements QuotationRepositoryPort {

  private final SpringDataQuotationRepository repository;
  private final SalePersistenceMapper mapper;

  @Override
  @Transactional
  public Quotation save(Quotation quotation) {
    QuotationEntity entity = mapper.toEntity(quotation);
    QuotationEntity saved = repository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Quotation> findByIdAndTenantId(Long id, String tenantId) {
    return repository.findByIdAndTenantId(id, tenantId).map(mapper::toDomain);
  }

  @Override
  public List<Quotation> findPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne) {
    List<QuotationEntity> entities =
        repository.findPaged(tenantId, cursorId, cursorCreatedAt, PageRequest.of(0, limitPlusOne));
    return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean confirmQuotation(String tenantId, Long id) {
    return repository.confirmQuotation(tenantId, id) > 0;
  }
}
