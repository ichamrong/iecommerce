package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.domain.repository.ShiftRepositoryPort;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.ShiftEntity;
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
  public CursorPage<Shift> findAll(String tenantId, String cursor, int limit) {
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

    Slice<ShiftEntity> slice =
        repository.findPaged(tenantId, cursorId, cursorTime, PageRequest.of(0, limit));

    String nextCursor = null;
    if (slice.hasNext()) {
      ShiftEntity last = slice.getContent().get(slice.getContent().size() - 1);
      String rawCursor = last.getCreatedAt() + ":" + last.getId();
      nextCursor = Base64.getEncoder().encodeToString(rawCursor.getBytes());
    }

    return new CursorPage<>(
        slice.getContent().stream().map(mapper::toDomain).toList(), nextCursor, slice.hasNext());
  }
}
