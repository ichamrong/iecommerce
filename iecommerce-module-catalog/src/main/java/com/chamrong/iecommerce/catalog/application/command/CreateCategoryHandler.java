package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.CategoryCreatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CategoryResponse;
import com.chamrong.iecommerce.catalog.application.dto.CreateCategoryRequest;
import com.chamrong.iecommerce.catalog.domain.Category;
import com.chamrong.iecommerce.catalog.domain.CategoryRepository;
import com.chamrong.iecommerce.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateCategoryHandler {

  private final CategoryRepository categoryRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public CategoryResponse handle(CreateCategoryRequest request) {
    var tenantId = TenantContext.requireTenantId();

    if (categoryRepository.existsByTenantIdAndSlug(tenantId, request.slug())) {
      throw new IllegalArgumentException("Category slug already exists: " + request.slug());
    }

    var category = new Category(tenantId, request.slug());
    category.setParentId(request.parentId());
    category.setSortOrder(request.sortOrder());
    category.setImageUrl(request.imageUrl());
    if (request.active()) {
      category.activate();
    } else {
      category.deactivate();
    }

    if (request.translations() != null) {
      request
          .translations()
          .forEach(
              (locale, trans) ->
                  category.upsertTranslation(locale, trans.name(), trans.description()));
    }

    var saved = categoryRepository.save(category);

    // We need to recompute the materialized path after save so the ID is available
    var parentPath = "/";
    if (request.parentId() != null) {
      var parent =
          categoryRepository
              .findById(request.parentId())
              .filter(p -> p.getTenantId().equals(tenantId))
              .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
      parentPath = parent.getMaterializedPath();
    }
    saved.rebuildPath(parentPath);
    categoryRepository.save(saved); // Second save to persist the path

    eventPublisher.publishEvent(new CategoryCreatedEvent(tenantId, saved.getId(), saved.getSlug()));

    return catalogMapper.toCategoryResponse(saved, "en");
  }
}
