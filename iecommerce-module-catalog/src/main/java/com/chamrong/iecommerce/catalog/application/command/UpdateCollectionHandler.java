package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.CollectionUpdatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CollectionResponse;
import com.chamrong.iecommerce.catalog.application.dto.UpdateCollectionRequest;
import com.chamrong.iecommerce.catalog.domain.CollectionRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCollectionHandler {

  private final CollectionRepository collectionRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public CollectionResponse handle(Long id, UpdateCollectionRequest request) {
    var tenantId = TenantContext.requireTenantId();
    var collection =
        collectionRepository
            .findById(id)
            .filter(c -> c.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Collection not found: " + id));

    if (request.slug() != null) {
      if (collectionRepository.existsByTenantIdAndSlugAndIdNot(tenantId, request.slug(), id)) {
        throw new IllegalArgumentException(
            "Collection with slug already exists: " + request.slug());
      }
      collection.setSlug(request.slug());
    }

    if (request.automatic() != null) collection.setAutomatic(request.automatic());
    if (request.rule() != null) collection.setRule(request.rule());
    if (request.sortOrder() != null) collection.setSortOrder(request.sortOrder());
    if (request.active() != null) collection.setActive(request.active());

    if (request.translations() != null) {
      request
          .translations()
          .forEach(
              (locale, dto) -> collection.upsertTranslation(locale, dto.name(), dto.description()));
    }

    var saved = collectionRepository.save(collection);
    eventPublisher.publishEvent(new CollectionUpdatedEvent(tenantId, id));
    return catalogMapper.toCollectionResponse(saved, "en");
  }
}
