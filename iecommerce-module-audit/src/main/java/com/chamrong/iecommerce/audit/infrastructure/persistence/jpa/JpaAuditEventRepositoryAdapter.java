package com.chamrong.iecommerce.audit.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditEventRepositoryPort;
import com.chamrong.iecommerce.audit.domain.ports.AuditSearchCriteria;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing AuditEventRepositoryPort using Spring Data JPA. All queries tenant-scoped.
 */
@Component
@RequiredArgsConstructor
public class JpaAuditEventRepositoryAdapter implements AuditEventRepositoryPort {

  private static final Sort KEYSET_SORT = Sort.by(Sort.Direction.DESC, "createdAt", "id");

  private final SpringDataAuditEventRepository springRepo;
  private final AuditEventEntityMapper mapper;

  @Override
  public AuditEvent save(AuditEvent event) {
    AuditEventEntity entity = mapper.toEntity(event);
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(Instant.now());
    }
    AuditEventEntity saved = springRepo.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<AuditEvent> findById(Long id) {
    return springRepo.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<AuditEvent> findFirstPage(
      String tenantId, AuditSearchCriteria filters, int limitPlusOne) {
    Specification<AuditEventEntity> spec =
        AuditQuerySpecifications.withTenantAndFilters(tenantId, filters);
    return springRepo
        .findAll(spec, PageRequest.of(0, limitPlusOne, KEYSET_SORT))
        .getContent()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<AuditEvent> findNextPage(
      String tenantId,
      AuditSearchCriteria filters,
      Instant cursorCreatedAt,
      Long cursorId,
      int limitPlusOne) {
    Specification<AuditEventEntity> spec =
        AuditQuerySpecifications.withTenantAndFilters(tenantId, filters)
            .and(AuditQuerySpecifications.keysetBefore(cursorCreatedAt, cursorId));
    return springRepo
        .findAll(spec, PageRequest.of(0, limitPlusOne, KEYSET_SORT))
        .getContent()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<String> findPreviousHashForTenant(String tenantId) {
    List<AuditEventEntity> latest =
        springRepo.findByTenantIdOrderByCreatedAtDescIdDesc(
            tenantId, PageRequest.of(0, 1, KEYSET_SORT));
    return latest.isEmpty() ? Optional.empty() : Optional.ofNullable(latest.get(0).getHash());
  }

  @Override
  public Optional<AuditEvent> findPreviousEventInChain(
      String tenantId, Instant createdAt, Long id) {
    List<AuditEventEntity> prev =
        springRepo.findPreviousInChain(tenantId, createdAt, id, PageRequest.of(0, 1));
    if (prev.isEmpty()) return Optional.empty();
    return Optional.of(mapper.toDomain(prev.get(0)));
  }
}
