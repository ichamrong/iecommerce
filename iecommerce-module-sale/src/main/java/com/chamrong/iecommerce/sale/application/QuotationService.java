package com.chamrong.iecommerce.sale.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.sale.application.dto.QuotationResponse;
import com.chamrong.iecommerce.sale.domain.Quotation;
import com.chamrong.iecommerce.sale.domain.repository.QuotationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationService {

  private final QuotationRepository quotationRepository;

  @Transactional
  public QuotationResponse createQuotation(
      String tenantId, String customerId, Instant expiryDate, String currency) {
    log.info("Creating quotation for customer {} for tenant {}", customerId, tenantId);

    Quotation quotation = new Quotation();
    quotation.setTenantId(tenantId);
    quotation.setCustomerId(customerId);
    quotation.setExpiryDate(expiryDate);
    quotation.setStatus(Quotation.QuotationStatus.DRAFT);
    quotation.setTotalAmount(new Money(java.math.BigDecimal.ZERO, currency));

    return toResponse(quotationRepository.save(quotation));
  }

  @Transactional
  public QuotationResponse updateStatus(Long id, Quotation.QuotationStatus status) {
    Quotation quotation =
        quotationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Quotation not found"));

    quotation.setStatus(status);
    return toResponse(quotationRepository.save(quotation));
  }

  private QuotationResponse toResponse(Quotation q) {
    List<QuotationResponse.QuotationItemResponse> items =
        q.getItems().stream()
            .map(
                i ->
                    new QuotationResponse.QuotationItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTotalPrice()))
            .toList();

    return new QuotationResponse(
        q.getId(),
        q.getCustomerId(),
        q.getExpiryDate(),
        q.getTotalAmount(),
        q.getStatus().name(),
        items);
  }
}
