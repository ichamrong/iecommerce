package com.chamrong.iecommerce.catalog.application.query;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.CollectionResponse;
import com.chamrong.iecommerce.catalog.domain.CollectionRepository;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionQueryHandler {

  private final CollectionRepository collectionRepository;
  private final CatalogMapper catalogMapper;

  public Page<CollectionResponse> listCollections(String locale, Pageable pageable) {
    var tenantId = TenantContext.requireTenantId();
    // Defaulting to filtering in-memory for basic version, or fetching all and mapping
    return collectionRepository
        .findAll(pageable)
        .map(c -> catalogMapper.toCollectionResponse(c, locale));
  }

  public CollectionResponse findById(Long id, String locale) {
    var tenantId = TenantContext.requireTenantId();
    return collectionRepository
        .findById(id)
        .filter(c -> c.getTenantId().equals(tenantId))
        .map(c -> catalogMapper.toCollectionResponse(c, locale))
        .orElseThrow(() -> new EntityNotFoundException("Collection not found: " + id));
  }

  public CollectionResponse findBySlug(String slug, String locale) {
    var tenantId = TenantContext.requireTenantId();
    return collectionRepository
        .findByTenantIdAndSlug(tenantId, slug)
        .map(c -> catalogMapper.toCollectionResponse(c, locale))
        .orElseThrow(() -> new EntityNotFoundException("Collection not found for slug: " + slug));
  }
}
