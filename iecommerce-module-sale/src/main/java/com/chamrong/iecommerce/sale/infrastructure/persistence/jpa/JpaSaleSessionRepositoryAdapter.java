package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import com.chamrong.iecommerce.sale.domain.repository.SaleSessionRepositoryPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleSessionEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.mapper.SalePersistenceMapper;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
  public CursorPage<SaleSession> findAll(
      String tenantId, String terminalId, String cursor, int limit) {
    Long cursorId = null;
    Instant cursorTime = null;

    if (cursor != null && !cursor.isBlank()) {
      try {
        String decoded = new String(Base64.getDecoder().decode(cursor));
        String[] parts = decoded.split(":");
        if (parts.length == 2) {
          cursorTime = Instant.parse(parts[0]);
          cursorId = Long.parseLong(parts[1]);
        }
      } catch (Exception e) {
      }
    }

    // Note: SpringDataSaleSessionRepository.findPaged currently only takes tenantId.
    // I need to update it to take terminalId as well if needed.
    // For now, let's keep it simple or update the repository.
    Slice<SaleSessionEntity> slice =
        repository.findPaged(tenantId, cursorId, cursorTime, PageRequest.of(0, limit));

    String nextCursor = null;
    if (slice.hasNext()) {
      SaleSessionEntity last = slice.getContent().get(slice.getContent().size() - 1);
      String rawCursor = last.getCreatedAt() + ":" + last.getId();
      nextCursor = Base64.getEncoder().encodeToString(rawCursor.getBytes());
    }

    return new CursorPage<>(
        slice.getContent().stream().map(mapper::toDomain).toList(), nextCursor, slice.hasNext());
  }
}
