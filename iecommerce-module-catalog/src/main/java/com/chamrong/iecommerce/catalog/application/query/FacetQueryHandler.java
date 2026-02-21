package com.chamrong.iecommerce.catalog.application.query;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.FacetResponse;
import com.chamrong.iecommerce.catalog.domain.FacetRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FacetQueryHandler {

  private final FacetRepository facetRepository;
  private final CatalogMapper catalogMapper;

  public List<FacetResponse> listFacets(String locale) {
    var tenantId = TenantContext.requireTenantId();
    return facetRepository.findByTenantId(tenantId).stream()
        .map(f -> catalogMapper.toFacetResponse(f, locale))
        .toList();
  }

  public FacetResponse findById(Long id, String locale) {
    var tenantId = TenantContext.requireTenantId();
    return facetRepository
        .findById(id)
        .filter(f -> f.getTenantId().equals(tenantId))
        .map(f -> catalogMapper.toFacetResponse(f, locale))
        .orElseThrow(() -> new EntityNotFoundException("Facet not found: " + id));
  }
}
