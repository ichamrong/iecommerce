package com.chamrong.iecommerce.sale.domain.service;

import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import com.chamrong.iecommerce.sale.domain.model.ReturnItem;
import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validates return quantities against original order lines (BG11). Algorithm: Map original lines by
 * ID for O(1) checks.
 */
public class ReturnValidator {

  public static void validateQuantities(
      SaleReturn saleReturn, Map<Long, BigDecimal> purchasedQuantities) {
    Map<Long, BigDecimal> returningQuantities =
        saleReturn.getItems().stream()
            .collect(
                Collectors.toMap(
                    ReturnItem::getOriginalLineId, ReturnItem::getQuantity, BigDecimal::add));

    returningQuantities.forEach(
        (lineId, qty) -> {
          BigDecimal purchasedQty = purchasedQuantities.get(lineId);
          if (purchasedQty == null) {
            throw new SaleDomainException("Original order line not found: " + lineId);
          }
          if (qty.compareTo(purchasedQty) > 0) {
            throw new SaleDomainException(
                "Return quantity exceeds purchased quantity for line: " + lineId);
          }
        });
  }
}
