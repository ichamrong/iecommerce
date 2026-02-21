package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.FacetCreatedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CreateFacetRequest;
import com.chamrong.iecommerce.catalog.application.dto.FacetResponse;
import com.chamrong.iecommerce.catalog.domain.Facet;
import com.chamrong.iecommerce.catalog.domain.FacetRepository;
import com.chamrong.iecommerce.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateFacetHandler {

  private final FacetRepository facetRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public FacetResponse handle(CreateFacetRequest request) {
    var tenantId = TenantContext.requireTenantId();

    var facet = new Facet(tenantId, request.code());
    facet.setFilterable(request.filterable());

    if (request.translationNames() != null) {
      request.translationNames().forEach(facet::upsertTranslation);
    }

    var saved = facetRepository.save(facet);
    eventPublisher.publishEvent(new FacetCreatedEvent(tenantId, saved.getId(), saved.getCode()));
    return catalogMapper.toFacetResponse(saved, "en");
  }
}
