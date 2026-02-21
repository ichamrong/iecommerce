package com.chamrong.iecommerce.catalog.application.query;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CategoryResponse;
import com.chamrong.iecommerce.catalog.domain.Category;
import com.chamrong.iecommerce.catalog.domain.CategoryRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side queries for categories. Builds tree structures from flat lists. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryQueryHandler {

  private final CategoryRepository categoryRepository;
  private final CatalogMapper mapper;

  /**
   * Returns the full category tree rooted at top-level nodes, with children nested recursively.
   * Efficient single-query approach: fetch ALL categories for the tenant and build tree in memory.
   */
  public List<CategoryResponse> getTree(String locale) {
    var tenantId = TenantContext.requireTenantId();
    var all =
        categoryRepository.findAll().stream()
            .filter(c -> c.getTenantId().equals(tenantId))
            .toList();
    return buildTree(all, null, locale);
  }

  /** Single category by ID — flat (no children). */
  public CategoryResponse getById(Long id, String locale) {
    var tenantId = TenantContext.requireTenantId();
    var category =
        categoryRepository
            .findById(id)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
    return mapper.toCategoryResponse(category, locale);
  }

  /**
   * Breadcrumb trail from a leaf to the root. Uses the materializedPath to load ancestors
   * efficiently: "/1/4/12/" → loads IDs [1, 4, 12] in one query.
   */
  public List<CategoryResponse> getBreadcrumb(Long leafId, String locale) {
    var tenantId = TenantContext.requireTenantId();
    var leaf =
        categoryRepository
            .findById(leafId)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + leafId));

    // Parse ancestor IDs from the materialized path
    var path = leaf.getMaterializedPath(); // e.g. "/1/4/12/"
    if (path.isEmpty()) return List.of(mapper.toCategoryResponse(leaf, locale));

    var segments = path.replaceAll("^/|/$", "").split("/");
    var breadcrumb = new ArrayList<CategoryResponse>();
    for (var segment : segments) {
      try {
        var ancestorId = Long.parseLong(segment);
        categoryRepository
            .findById(ancestorId)
            .ifPresent(c -> breadcrumb.add(mapper.toCategoryResponse(c, locale)));
      } catch (NumberFormatException ignored) {
      }
    }
    return breadcrumb;
  }

  // ── Tree builder ─────────────────────────────────────────────────────────

  private List<CategoryResponse> buildTree(List<Category> all, Long parentId, String locale) {
    return all.stream()
        .filter(c -> java.util.Objects.equals(c.getParentId(), parentId))
        .sorted(java.util.Comparator.comparingInt(Category::getSortOrder))
        .map(
            c -> {
              var base = mapper.toCategoryResponse(c, locale);
              var children = buildTree(all, c.getId(), locale);
              return new CategoryResponse(
                  base.id(),
                  base.slug(),
                  base.parentId(),
                  base.materializedPath(),
                  base.depth(),
                  base.sortOrder(),
                  base.imageUrl(),
                  base.active(),
                  base.resolvedLocale(),
                  base.name(),
                  base.description(),
                  children.isEmpty() ? null : children);
            })
        .toList();
  }
}
