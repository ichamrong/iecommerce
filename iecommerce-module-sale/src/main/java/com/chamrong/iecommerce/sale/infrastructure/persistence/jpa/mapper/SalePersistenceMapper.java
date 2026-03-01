package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.mapper;

import com.chamrong.iecommerce.sale.domain.model.Quotation;
import com.chamrong.iecommerce.sale.domain.model.QuotationItem;
import com.chamrong.iecommerce.sale.domain.model.ReturnItem;
import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.QuotationEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.QuotationItemEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.ReturnItemEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleReturnEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.SaleSessionEntity;
import com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity.ShiftEntity;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Manual mapper between Domain Models and JPA Entities. */
@Component
public class SalePersistenceMapper {

  // --- Quotation ---

  public Quotation toDomain(QuotationEntity entity) {
    if (entity == null) return null;
    Quotation quotation =
        new Quotation(
            entity.getId(),
            entity.getTenantId(),
            entity.getVersion(),
            entity.getCustomerId(),
            entity.getExpiryDate(),
            entity.getStatus(),
            entity.getTotalAmount(),
            entity.getItems().stream().map(this::toDomain).collect(Collectors.toList()),
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    // Link back items
    for (QuotationItem item : quotation.getItems()) {
      // In the pure domain model, items already have a reference if set correctly,
      // but since we recreated them, we might need to ensure they match.
    }
    return quotation;
  }

  public QuotationItem toDomain(QuotationItemEntity entity) {
    if (entity == null) return null;
    return new QuotationItem(
        entity.getId(),
        null, // Quotation back-ref will be handled by aggregate
        entity.getProductId(),
        entity.getQuantity(),
        entity.getUnitPrice(),
        entity.getTotalPrice());
  }

  public QuotationEntity toEntity(Quotation domain) {
    if (domain == null) return null;
    QuotationEntity entity = new QuotationEntity();
    entity.setId(domain.getId());
    entity.setTenantId(domain.getTenantId());
    entity.setVersion(domain.getVersion());
    entity.setCustomerId(domain.getCustomerId());
    entity.setExpiryDate(domain.getExpiryDate());
    entity.setStatus(domain.getStatus());
    entity.setTotalAmount(domain.getTotalAmount());
    entity.setItems(
        domain.getItems().stream().map(i -> toEntity(i, entity)).collect(Collectors.toList()));
    return entity;
  }

  public QuotationItemEntity toEntity(QuotationItem domain, QuotationEntity quotationEntity) {
    if (domain == null) return null;
    QuotationItemEntity entity = new QuotationItemEntity();
    entity.setId(domain.getId());
    entity.setQuotation(quotationEntity);
    entity.setProductId(domain.getProductId());
    entity.setQuantity(domain.getQuantity());
    entity.setUnitPrice(domain.getUnitPrice());
    entity.setTotalPrice(domain.getTotalPrice());
    return entity;
  }

  // --- Session ---

  public SaleSession toDomain(SaleSessionEntity entity) {
    if (entity == null) return null;
    return new SaleSession(
        entity.getId(),
        entity.getTenantId(),
        entity.getVersion(),
        toDomain(entity.getShift()),
        entity.getCashierId(),
        entity.getTerminalId(),
        entity.getStartTime(),
        entity.getEndTime(),
        entity.getStatus(),
        entity.getExpectedAmount(),
        entity.getActualAmount(),
        entity.getCreatedAt());
  }

  public SaleSessionEntity toEntity(SaleSession domain) {
    if (domain == null) return null;
    SaleSessionEntity entity = new SaleSessionEntity();
    entity.setId(domain.getId());
    entity.setTenantId(domain.getTenantId());
    entity.setVersion(domain.getVersion());
    entity.setShift(toEntity(domain.getShift()));
    entity.setCashierId(domain.getCashierId());
    entity.setTerminalId(domain.getTerminalId());
    entity.setStartTime(domain.getStartTime());
    entity.setEndTime(domain.getEndTime());
    entity.setStatus(domain.getStatus());
    entity.setExpectedAmount(domain.getExpectedAmount());
    entity.setActualAmount(domain.getActualAmount());
    return entity;
  }

  // --- Shift ---

  public Shift toDomain(ShiftEntity entity) {
    if (entity == null) return null;
    return new Shift(
        entity.getId(),
        entity.getTenantId(),
        entity.getVersion(),
        entity.getStaffId(),
        entity.getTerminalId(),
        entity.getStartTime(),
        entity.getEndTime(),
        entity.getStatus(),
        entity.getCreatedAt());
  }

  public ShiftEntity toEntity(Shift domain) {
    if (domain == null) return null;
    ShiftEntity entity = new ShiftEntity();
    entity.setId(domain.getId());
    entity.setTenantId(domain.getTenantId());
    entity.setVersion(domain.getVersion());
    entity.setStaffId(domain.getStaffId());
    entity.setTerminalId(domain.getTerminalId());
    entity.setStartTime(domain.getStartTime());
    entity.setEndTime(domain.getEndTime());
    entity.setStatus(domain.getStatus());
    return entity;
  }

  // --- Return ---

  public SaleReturn toDomain(SaleReturnEntity entity) {
    if (entity == null) return null;
    return new SaleReturn(
        entity.getId(),
        entity.getTenantId(),
        entity.getVersion(),
        entity.getOriginalOrderId(),
        entity.getReturnKey(),
        entity.getStatus(),
        entity.getReason(),
        entity.getTotalRefundAmount(),
        entity.getItems().stream().map(this::toDomain).collect(Collectors.toList()),
        entity.getRequestedAt(),
        entity.getCompletedAt(),
        entity.getCreatedAt());
  }

  public ReturnItem toDomain(ReturnItemEntity entity) {
    if (entity == null) return null;
    return new ReturnItem(
        entity.getId(),
        null,
        entity.getOriginalLineId(),
        entity.getQuantity(),
        entity.getRefundPrice(),
        entity.getTotalRefundAmount());
  }

  public SaleReturnEntity toEntity(SaleReturn domain) {
    if (domain == null) return null;
    SaleReturnEntity entity = new SaleReturnEntity();
    entity.setId(domain.getId());
    entity.setTenantId(domain.getTenantId());
    entity.setVersion(domain.getVersion());
    entity.setOriginalOrderId(domain.getOriginalOrderId());
    entity.setReturnKey(domain.getReturnKey());
    entity.setStatus(domain.getStatus());
    entity.setReason(domain.getReason());
    entity.setTotalRefundAmount(domain.getTotalAmount());
    entity.setRequestedAt(domain.getRequestedAt());
    entity.setCompletedAt(domain.getCompletedAt());
    entity.setItems(
        domain.getItems().stream().map(i -> toEntity(i, entity)).collect(Collectors.toList()));
    return entity;
  }

  public ReturnItemEntity toEntity(ReturnItem domain, SaleReturnEntity saleReturnEntity) {
    if (domain == null) return null;
    ReturnItemEntity entity = new ReturnItemEntity();
    entity.setId(domain.getId());
    entity.setSaleReturn(saleReturnEntity);
    entity.setOriginalLineId(domain.getOriginalLineId());
    entity.setQuantity(domain.getQuantity());
    entity.setRefundPrice(domain.getRefundPrice());
    entity.setTotalRefundAmount(domain.getTotalRefundAmount());
    return entity;
  }
}
