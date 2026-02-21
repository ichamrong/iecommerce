package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.FacetValueAddedEvent;
import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.AddFacetValueRequest;
import com.chamrong.iecommerce.catalog.application.dto.FacetResponse;
import com.chamrong.iecommerce.catalog.domain.FacetRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddFacetValueHandler {

  private final FacetRepository facetRepository;
  private final CatalogMapper catalogMapper;
  private final ApplicationEventPublisher eventPublisher;

  public FacetResponse handle(Long facetId, AddFacetValueRequest request) {
    var tenantId = TenantContext.requireTenantId();

    var facet =
        facetRepository
            .findById(facetId)
            .filter(f -> f.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Facet not found: " + facetId));

    var facetValue = facet.addValue(request.code());
    if (request.translationValues() != null) {
      request.translationValues().forEach(facetValue::upsertTranslation);
    }

    var saved = facetRepository.save(facet);
    eventPublisher.publishEvent(new FacetValueAddedEvent(tenantId, facetId, request.code()));
    return catalogMapper.toFacetResponse(saved, "en");
  }
}
