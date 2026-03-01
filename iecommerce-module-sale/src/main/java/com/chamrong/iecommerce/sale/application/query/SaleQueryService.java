package com.chamrong.iecommerce.sale.application.query;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.sale.application.dto.QuotationResponse;
import com.chamrong.iecommerce.sale.application.dto.SaleReturnResponse;
import com.chamrong.iecommerce.sale.application.dto.SaleSessionResponse;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import com.chamrong.iecommerce.sale.domain.model.Quotation;
import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.domain.ports.QuotationRepositoryPort;
import com.chamrong.iecommerce.sale.domain.ports.SaleReturnRepositoryPort;
import com.chamrong.iecommerce.sale.domain.ports.SaleSessionRepositoryPort;
import com.chamrong.iecommerce.sale.domain.ports.ShiftRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Endpoint keys for filterHash binding (cursor not reusable across endpoints). */
final class SaleListEndpointKeys {
  static final String LIST_SHIFTS = "sale:listShifts";
  static final String LIST_SESSIONS = "sale:listSessions";
  static final String LIST_QUOTATIONS = "sale:listQuotations";
  static final String LIST_RETURNS = "sale:listReturns";
}

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleQueryService {

  private final QuotationRepositoryPort quotationRepository;
  private final SaleSessionRepositoryPort sessionRepository;
  private final ShiftRepositoryPort shiftRepository;
  private final SaleReturnRepositoryPort returnRepository;

  @Transactional(readOnly = true)
  public CursorPageResponse<QuotationResponse> listQuotations(
      String tenantId, String cursor, int limit, Map<String, Object> filters) {
    String filterHash = FilterHasher.computeHash(SaleListEndpointKeys.LIST_QUOTATIONS, filters);
    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new com.chamrong.iecommerce.common.pagination.InvalidCursorException(
            com.chamrong.iecommerce.common.pagination.InvalidCursorException.INVALID_CURSOR,
            "Invalid cursor id");
      }
    }
    int limitPlusOne = Math.min(100, Math.max(1, limit)) + 1;
    List<Quotation> list =
        quotationRepository.findPage(tenantId, cursorCreatedAt, cursorId, limitPlusOne);
    return buildResponse(
        list,
        limit,
        filterHash,
        SaleQueryService::toQuotationResponse,
        Quotation::getCreatedAt,
        Quotation::getId);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<SaleSessionResponse> listSessions(
      String tenantId, String terminalId, String cursor, int limit, Map<String, Object> filters) {
    String filterHash = FilterHasher.computeHash(SaleListEndpointKeys.LIST_SESSIONS, filters);
    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new com.chamrong.iecommerce.common.pagination.InvalidCursorException(
            com.chamrong.iecommerce.common.pagination.InvalidCursorException.INVALID_CURSOR,
            "Invalid cursor id");
      }
    }
    int limitPlusOne = Math.min(100, Math.max(1, limit)) + 1;
    List<SaleSession> list =
        sessionRepository.findPage(tenantId, terminalId, cursorCreatedAt, cursorId, limitPlusOne);
    return buildResponse(
        list,
        limit,
        filterHash,
        SaleQueryService::toSessionResponse,
        SaleSession::getCreatedAt,
        SaleSession::getId);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<ShiftResponse> listShifts(
      String tenantId, String cursor, int limit, Map<String, Object> filters) {
    String filterHash = FilterHasher.computeHash(SaleListEndpointKeys.LIST_SHIFTS, filters);
    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new com.chamrong.iecommerce.common.pagination.InvalidCursorException(
            com.chamrong.iecommerce.common.pagination.InvalidCursorException.INVALID_CURSOR,
            "Invalid cursor id");
      }
    }
    int limitPlusOne = Math.min(100, Math.max(1, limit)) + 1;
    List<Shift> list = shiftRepository.findPage(tenantId, cursorCreatedAt, cursorId, limitPlusOne);
    return buildResponse(
        list,
        limit,
        filterHash,
        SaleQueryService::toShiftResponse,
        Shift::getCreatedAt,
        Shift::getId);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<SaleReturnResponse> listReturns(
      String tenantId, String cursor, int limit, Map<String, Object> filters) {
    String filterHash = FilterHasher.computeHash(SaleListEndpointKeys.LIST_RETURNS, filters);
    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new com.chamrong.iecommerce.common.pagination.InvalidCursorException(
            com.chamrong.iecommerce.common.pagination.InvalidCursorException.INVALID_CURSOR,
            "Invalid cursor id");
      }
    }
    int limitPlusOne = Math.min(100, Math.max(1, limit)) + 1;
    List<SaleReturn> list =
        returnRepository.findPage(tenantId, cursorCreatedAt, cursorId, limitPlusOne);
    return buildResponse(
        list,
        limit,
        filterHash,
        SaleQueryService::toReturnResponse,
        SaleReturn::getCreatedAt,
        SaleReturn::getId);
  }

  private static <T, R> CursorPageResponse<R> buildResponse(
      List<T> list,
      int limit,
      String filterHash,
      java.util.function.Function<T, R> toResponse,
      java.util.function.Function<T, Instant> getCreatedAt,
      java.util.function.Function<T, Long> getId) {
    int effectiveLimit = Math.min(100, Math.max(1, limit));
    boolean hasNext = list.size() > effectiveLimit;
    List<T> pageData = hasNext ? list.subList(0, effectiveLimit) : list;
    List<R> data = pageData.stream().map(toResponse).toList();
    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      T last = pageData.get(pageData.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(
                  1, getCreatedAt.apply(last), String.valueOf(getId.apply(last)), filterHash));
    }
    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

  public static SaleSessionResponse toSessionResponse(SaleSession s) {
    return new SaleSessionResponse(
        s.getId(),
        s.getShift().getId(),
        s.getCashierId(),
        s.getTerminalId(),
        s.getStartTime(),
        s.getEndTime(),
        s.getStatus().name(),
        s.getExpectedAmount(),
        s.getActualAmount());
  }

  private static QuotationResponse toQuotationResponse(Quotation q) {
    return new QuotationResponse(
        q.getId(),
        q.getCustomerId(),
        q.getExpiryDate(),
        q.getTotalAmount(),
        q.getStatus().name(),
        q.getItems().stream()
            .map(
                i ->
                    new QuotationResponse.QuotationItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTotalPrice()))
            .toList());
  }

  private static ShiftResponse toShiftResponse(Shift s) {
    return new ShiftResponse(
        s.getId(),
        s.getStaffId(),
        s.getTerminalId(),
        s.getStartTime(),
        s.getEndTime(),
        s.getStatus().name());
  }

  private static SaleReturnResponse toReturnResponse(SaleReturn r) {
    return new SaleReturnResponse(
        r.getId(),
        r.getOriginalOrderId(),
        r.getReturnKey(),
        r.getStatus().name(),
        r.getReason(),
        r.getTotalRefundAmount(),
        r.getRequestedAt(),
        r.getCompletedAt(),
        r.getItems().stream()
            .map(
                i ->
                    new SaleReturnResponse.ReturnItemResponse(
                        i.getId(),
                        i.getOriginalLineId(),
                        i.getQuantity(),
                        i.getRefundPrice(),
                        i.getTotalRefundAmount()))
            .toList());
  }
}
