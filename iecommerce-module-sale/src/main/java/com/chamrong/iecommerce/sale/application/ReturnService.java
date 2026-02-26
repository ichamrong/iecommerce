package com.chamrong.iecommerce.sale.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.application.dto.ReturnResponse;
import com.chamrong.iecommerce.sale.domain.SaleReturn;
import com.chamrong.iecommerce.sale.domain.repository.SaleReturnRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnService {

  private final SaleReturnRepository returnRepository;

  @Transactional
  public ReturnResponse createReturn(
      String tenantId, String orderId, String reason, Money refundAmount) {
    log.info("Creating return for order {} for tenant {}", orderId, tenantId);

    SaleReturn saleReturn = new SaleReturn();
    saleReturn.setTenantId(tenantId);
    saleReturn.setOrderId(orderId);
    saleReturn.setReason(reason);
    saleReturn.setRefundAmount(refundAmount);
    saleReturn.setStatus(SaleReturn.ReturnStatus.PENDING);

    return toResponse(returnRepository.save(saleReturn));
  }

  @Transactional
  public ReturnResponse updateStatus(Long id, SaleReturn.ReturnStatus status) {
    SaleReturn saleReturn =
        returnRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Return not found"));

    saleReturn.setStatus(status);
    return toResponse(returnRepository.save(saleReturn));
  }

  private ReturnResponse toResponse(SaleReturn r) {
    List<ReturnResponse.ReturnItemResponse> items =
        r.getItems().stream()
            .map(
                i ->
                    new ReturnResponse.ReturnItemResponse(
                        i.getId(), i.getProductId(), i.getQuantity(), i.getCondition()))
            .toList();

    return new ReturnResponse(
        r.getId(), r.getOrderId(), r.getReason(), r.getRefundAmount(), r.getStatus().name(), items);
  }
}
