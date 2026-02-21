package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.CollectionCreatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CollectionResponse;
import com.chamrong.iecommerce.catalog.application.dto.CreateCollectionRequest;
import com.chamrong.iecommerce.catalog.domain.Collection;
import com.chamrong.iecommerce.catalog.domain.CollectionRepository;
import com.chamrong.iecommerce.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateCollectionHandler {

  private final CollectionRepository collectionRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public CollectionResponse handle(CreateCollectionRequest request) {
    var tenantId = TenantContext.requireTenantId();

    if (collectionRepository.findByTenantIdAndSlug(tenantId, request.slug()).isPresent()) {
      throw new IllegalArgumentException("Collection with slug already exists: " + request.slug());
    }

    var collection = new Collection(tenantId, request.slug());
    collection.setAutomatic(request.automatic());
    collection.setRule(request.rule());
    collection.setSortOrder(request.sortOrder());
    collection.setActive(request.active());

    if (request.translations() != null) {
      request
          .translations()
          .forEach(
              (locale, dto) -> collection.upsertTranslation(locale, dto.name(), dto.description()));
    }

    var saved = collectionRepository.save(collection);
    eventPublisher.publishEvent(
        new CollectionCreatedEvent(tenantId, saved.getId(), saved.getSlug()));
    return catalogMapper.toCollectionResponse(saved, "en"); // default resolution
  }
}
