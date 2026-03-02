package com.chamrong.iecommerce.promotion.application.query;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageRequest;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.promotion.application.dto.PromotionResponse;
import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side service for listing promotions using shared cursor pagination.
 *
 * <p>This service is responsible for:
 *
 * <ul>
 *   <li>Binding filter parameters into a stable {@code filterHash}
 *   <li>Decoding and validating cursors via {@link CursorCodec}
 *   <li>Delegating keyset queries to {@link PromotionRepository}
 *   <li>Mapping domain models to {@link PromotionResponse} DTOs
 * </ul>
 */
@Service
public class PromotionQueryService {

  private static final Logger log = LoggerFactory.getLogger(PromotionQueryService.class);

  private static final String PROMOTION_LIST_ENDPOINT = "promotion:list";

  private final PromotionRepository promotionRepository;

  public PromotionQueryService(PromotionRepository promotionRepository) {
    this.promotionRepository = promotionRepository;
  }

  /**
   * Lists promotions for the given tenant with cursor pagination.
   *
   * @param tenantId current tenant scope (required)
   * @param status optional promotion status filter
   * @param pageRequest cursor page request (cursor, limit, filters)
   * @return {@link CursorPageResponse} of {@link PromotionResponse}
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<PromotionResponse> listPromotions(
      String tenantId, PromotionStatus status, CursorPageRequest pageRequest) {

    Map<String, Object> filters = buildFilters(tenantId, status, pageRequest);
    String filterHash = FilterHasher.computeHash(PROMOTION_LIST_ENDPOINT, filters);

    Instant createdAtCursor = null;
    Long idCursor = null;
    String rawCursor = pageRequest.getCursor();

    if (rawCursor != null && !rawCursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(rawCursor, filterHash);
      createdAtCursor = payload.getCreatedAt();
      if (payload.getId() != null && !payload.getId().isBlank()) {
        try {
          idCursor = Long.parseLong(payload.getId());
        } catch (NumberFormatException ex) {
          log.warn("Invalid id in cursor payload for tenant {}: {}", tenantId, payload.getId());
        }
      }
    }

    int limit = pageRequest.getLimit();
    int limitPlusOne = pageRequest.getLimitPlusOne();

    List<Promotion> results =
        promotionRepository.findPage(tenantId, status, createdAtCursor, idCursor, limitPlusOne);

    boolean hasNext = results.size() > limit;
    List<Promotion> pageData = hasNext ? results.subList(0, limit) : results;

    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      Promotion last = pageData.get(pageData.size() - 1);
      CursorPayload payload =
          new CursorPayload(
              1,
              last.getCreatedAt(),
              String.valueOf(last.getId()),
              filterHash == null ? "" : filterHash);
      nextCursor = CursorCodec.encode(payload);
    }

    List<PromotionResponse> data =
        pageData.stream().map(PromotionQueryService::mapToResponse).collect(Collectors.toList());

    return CursorPageResponse.of(data, nextCursor, hasNext, limit);
  }

  private Map<String, Object> buildFilters(
      String tenantId, PromotionStatus status, CursorPageRequest pageRequest) {
    Map<String, Object> filters = new HashMap<>();
    filters.put("tenantId", tenantId);
    if (status != null) {
      filters.put("status", status.name());
    }
    if (pageRequest.getFilters() != null) {
      filters.putAll(pageRequest.getFilters());
    }
    return filters;
  }

  private static PromotionResponse mapToResponse(Promotion p) {
    return new PromotionResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getType(),
        p.getValue(),
        p.getCode(),
        p.getValidFrom(),
        p.getValidTo(),
        p.getStatus(),
        p.getPriority(),
        p.isStackable(),
        p.getUsageLimit(),
        p.getUsedCount());
  }
}
