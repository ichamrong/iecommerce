package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.CategoryUpdatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CategoryResponse;
import com.chamrong.iecommerce.catalog.application.dto.UpdateCategoryRequest;
import com.chamrong.iecommerce.catalog.domain.Category;
import com.chamrong.iecommerce.catalog.domain.CategoryRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCategoryHandler {

  private final CategoryRepository categoryRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public CategoryResponse handle(Long id, UpdateCategoryRequest request, String locale) {
    var tenantId = TenantContext.requireTenantId();

    var category =
        categoryRepository
            .findById(id)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

    if (request.slug() != null) {
      if (categoryRepository.existsByTenantIdAndSlugAndIdNot(tenantId, request.slug(), id)) {
        throw new IllegalArgumentException("Category slug already exists: " + request.slug());
      }
      category.setSlug(request.slug());
    }

    if (request.sortOrder() != null) category.setSortOrder(request.sortOrder());
    if (request.imageUrl() != null) category.setImageUrl(request.imageUrl());
    if (request.active() != null) {
      if (request.active()) category.activate();
      else category.deactivate();
    }

    if (request.translations() != null) {
      request
          .translations()
          .forEach(
              (loc, trans) -> category.upsertTranslation(loc, trans.name(), trans.description()));
    }

    boolean parentChanged =
        request.parentId() != null && !request.parentId().equals(category.getParentId());

    if (parentChanged) {
      category.setParentId(request.parentId());
      var parentPath = "/";
      var parent =
          categoryRepository
              .findById(request.parentId())
              .filter(p -> p.getTenantId().equals(tenantId))
              .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
      parentPath = parent.getMaterializedPath();
      category.rebuildPath(parentPath);

      // Update all children recursively
      updateChildrenPaths(category, tenantId);
    }

    var saved = categoryRepository.save(category);
    eventPublisher.publishEvent(new CategoryUpdatedEvent(tenantId, saved.getId(), saved.getSlug()));
    return catalogMapper.toCategoryResponse(saved, locale);
  }

  private void updateChildrenPaths(Category parent, String tenantId) {
    // In a real application, you might want to do this via a bulk SQL update for performance
    // if the tree is large.
    var children =
        categoryRepository.findByTenantIdAndParentIdOrderBySortOrderAsc(tenantId, parent.getId());
    for (var child : children) {
      child.rebuildPath(parent.getMaterializedPath());
      categoryRepository.save(child);
      updateChildrenPaths(child, tenantId);
    }
  }
}
