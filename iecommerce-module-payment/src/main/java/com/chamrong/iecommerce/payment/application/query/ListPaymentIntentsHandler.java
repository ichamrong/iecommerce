package com.chamrong.iecommerce.payment.application.query;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handler for listing payment intents with shared cursor pagination. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListPaymentIntentsHandler {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;
  private static final String ENDPOINT_LIST_INTENTS = "payment:listIntents";

  private final PaymentIntentRepositoryPort repository;

  /**
   * Returns a cursor-paginated list of payment intents for a tenant.
   *
   * @param tenantId tenant scope
   * @param cursor opaque cursor; null/blank = first page
   * @param limit requested page size
   */
  public CursorPageResponse<PaymentIntent> handle(String tenantId, String cursor, int limit) {
    int effectiveLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
    int fetchLimit = effectiveLimit + 1;

    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_INTENTS, filterMap);

    Instant lastCreatedAt = null;
    UUID lastId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      lastCreatedAt = payload.getCreatedAt();
      try {
        lastId = UUID.fromString(payload.getId());
      } catch (IllegalArgumentException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<PaymentIntent> intents =
        repository.findNextPage(tenantId, lastCreatedAt, lastId, fetchLimit);

    boolean hasNext = intents.size() > effectiveLimit;
    List<PaymentIntent> page = hasNext ? intents.subList(0, effectiveLimit) : intents;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      PaymentIntent last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), last.getIntentId().toString(), filterHash));
    }

    return CursorPageResponse.of(page, nextCursor, hasNext, effectiveLimit);
  }
}
