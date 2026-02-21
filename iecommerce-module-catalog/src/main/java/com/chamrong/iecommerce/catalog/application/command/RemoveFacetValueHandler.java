package com.chamrong.iecommerce.catalog.application.command;

import com.chamrong.iecommerce.catalog.FacetValueRemovedEvent;
import com.chamrong.iecommerce.catalog.domain.FacetRepository;
import com.chamrong.iecommerce.catalog.domain.FacetValue;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RemoveFacetValueHandler {

  private final FacetRepository facetRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long facetId, Long valueId) {
    var tenantId = TenantContext.requireTenantId();

    var facet =
        facetRepository
            .findById(facetId)
            .filter(f -> f.getTenantId().equals(tenantId))
            .orElseThrow(() -> new EntityNotFoundException("Facet not found: " + facetId));

    var valueCode =
        facet.getValues().stream()
            .filter(v -> v.getId().equals(valueId))
            .map(FacetValue::getCode)
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("FacetValue not found: " + valueId));

    facet.removeValue(valueId);
    facetRepository.save(facet);
    eventPublisher.publishEvent(new FacetValueRemovedEvent(tenantId, facetId, valueCode));
  }
}
