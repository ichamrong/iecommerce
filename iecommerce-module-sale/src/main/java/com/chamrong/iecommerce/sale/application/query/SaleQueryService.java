package com.chamrong.iecommerce.sale.application.query;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.application.dto.QuotationResponse;
import com.chamrong.iecommerce.sale.application.dto.SaleReturnResponse;
import com.chamrong.iecommerce.sale.application.dto.SaleSessionResponse;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import com.chamrong.iecommerce.sale.domain.repository.QuotationRepositoryPort;
import com.chamrong.iecommerce.sale.domain.repository.SaleReturnRepositoryPort;
import com.chamrong.iecommerce.sale.domain.repository.SaleSessionRepositoryPort;
import com.chamrong.iecommerce.sale.domain.repository.ShiftRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaleQueryService {

  private final QuotationRepositoryPort quotationRepository;
  private final SaleSessionRepositoryPort sessionRepository;
  private final ShiftRepositoryPort shiftRepository;
  private final SaleReturnRepositoryPort returnRepository;

  @Transactional(readOnly = true)
  public CursorPage<QuotationResponse> listQuotations(String tenantId, String cursor, int limit) {
    var page = quotationRepository.findAll(tenantId, cursor, limit);
    return new CursorPage<>(
        page.getData().stream().map(this::toQuotationResponse).toList(),
        page.getNextCursor(),
        page.isHasMore());
  }

  @Transactional(readOnly = true)
  public CursorPage<SaleSessionResponse> listSessions(
      String tenantId, String terminalId, String cursor, int limit) {
    var page = sessionRepository.findAll(tenantId, terminalId, cursor, limit);
    return new CursorPage<>(
        page.getData().stream().map(this::toSessionResponse).toList(),
        page.getNextCursor(),
        page.isHasMore());
  }

  @Transactional(readOnly = true)
  public CursorPage<ShiftResponse> listShifts(String tenantId, String cursor, int limit) {
    var page = shiftRepository.findAll(tenantId, cursor, limit);
    return new CursorPage<>(
        page.getData().stream().map(this::toShiftResponse).toList(),
        page.getNextCursor(),
        page.isHasMore());
  }

  @Transactional(readOnly = true)
  public CursorPage<SaleReturnResponse> listReturns(String tenantId, String cursor, int limit) {
    var page = returnRepository.findAll(tenantId, cursor, limit);
    return new CursorPage<>(
        page.getData().stream().map(this::toReturnResponse).toList(),
        page.getNextCursor(),
        page.isHasMore());
  }

  // Mappers (private)
  private QuotationResponse toQuotationResponse(
      com.chamrong.iecommerce.sale.domain.model.Quotation q) {
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

  public SaleSessionResponse toSessionResponse(
      com.chamrong.iecommerce.sale.domain.model.SaleSession s) {
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

  private ShiftResponse toShiftResponse(com.chamrong.iecommerce.sale.domain.model.Shift s) {
    return new ShiftResponse(
        s.getId(),
        s.getStaffId(),
        s.getTerminalId(),
        s.getStartTime(),
        s.getEndTime(),
        s.getStatus().name());
  }

  private SaleReturnResponse toReturnResponse(
      com.chamrong.iecommerce.sale.domain.model.SaleReturn r) {
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
