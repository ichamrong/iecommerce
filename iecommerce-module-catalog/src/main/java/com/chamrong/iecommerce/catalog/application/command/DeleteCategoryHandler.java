package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.CategoryDeletedEvent;
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
public class DeleteCategoryHandler {

  private final CategoryRepository categoryRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long id) {
    var tenantId = TenantContext.requireTenantId();

    var category =
        categoryRepository
            .findById(id)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

    // Simple delete for now. A more robust implementation might require
    // reassigning children to the parent or blocking deletion if products exist.
    categoryRepository.delete(category);
    eventPublisher.publishEvent(new CategoryDeletedEvent(tenantId, id));
  }
}
