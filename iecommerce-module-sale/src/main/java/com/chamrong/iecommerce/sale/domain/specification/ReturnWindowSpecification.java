package com.chamrong.iecommerce.sale.domain.specification;

import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReturnWindowSpecification implements Specification<SaleReturn> {

  private final int maxDays;
  private final Instant originalSaleDate;

  @Override
  public boolean isSatisfiedBy(SaleReturn saleReturn) {
    if (originalSaleDate == null) return false;
    Instant now = Instant.now();
    return originalSaleDate.plus(maxDays, ChronoUnit.DAYS).isAfter(now);
  }
}
