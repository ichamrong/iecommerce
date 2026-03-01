package com.chamrong.iecommerce.catalog.infrastructure.persistence;

import com.chamrong.iecommerce.catalog.domain.Category;
import com.chamrong.iecommerce.catalog.domain.ports.CategoryRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing {@link CategoryRepositoryPort}.
 *
 * <p>Cursor pagination backed by {@code idx_categories_cursor}.
 */
@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

  private final SpringDataCategoryRepository jpaRepo;

  @Override
  public Optional<Category> findById(Long id) {
    return jpaRepo.findById(id);
  }

  @Override
  public List<Category> findByTenantId(String tenantId) {
    return jpaRepo.findByTenantId(tenantId);
  }

  @Override
  public Category save(Category category) {
    return jpaRepo.save(category);
  }

  @Override
  public void delete(Category category) {
    jpaRepo.delete(category);
  }

  @Override
  public List<Category> findDescendants(String materializedPathPrefix, String tenantId) {
    return jpaRepo.findDescendants(tenantId, materializedPathPrefix + "%");
  }

  @Override
  public List<Category> findCursorPage(
      String tenantId, Instant afterCreatedAt, Long afterId, int limit) {
    if (afterCreatedAt == null || afterId == null) {
      return jpaRepo.findFirstPage(tenantId, PageRequest.of(0, limit));
    }
    return jpaRepo.findNextPage(tenantId, afterCreatedAt, afterId, PageRequest.of(0, limit));
  }
}
