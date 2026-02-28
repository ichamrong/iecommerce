package com.chamrong.iecommerce.staff.infrastructure.persistence;

import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link StaffRepositoryPort}.
 *
 * <p>Cursor pagination uses keyset semantics on (created_at DESC, id DESC) — O(log N) regardless of
 * page depth, backed by {@code idx_staff_cursor}.
 */
@Component
@RequiredArgsConstructor
public class StaffRepositoryAdapter implements StaffRepositoryPort {

  private final SpringDataStaffRepository jpaRepo;

  @Override
  public Optional<StaffProfile> findByUserId(String userId) {
    return jpaRepo.findByUserId(userId);
  }

  @Override
  public Optional<StaffProfile> findById(Long id) {
    return jpaRepo.findById(id);
  }

  @Override
  public boolean existsByUserId(String userId) {
    return jpaRepo.existsByUserId(userId);
  }

  @Override
  public StaffProfile save(StaffProfile profile) {
    return jpaRepo.save(profile);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Passes {@code limit} as a {@link PageRequest} so Spring Data enforces the SQL {@code LIMIT}
   * clause — fixing the prior bug where the inline {@code @Param("limit")} was silently ignored.
   */
  @Override
  public List<StaffProfile> findNextPage(Instant afterCreatedAt, Long afterId, int limit) {
    if (afterCreatedAt == null || afterId == null) {
      return jpaRepo.findFirstPage(PageRequest.of(0, limit));
    }
    return jpaRepo.findNextPage(afterCreatedAt, afterId, PageRequest.of(0, limit));
  }
}
