package com.chamrong.iecommerce.invoice.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateInvoiceRequest(
    Long orderId,
    String currency,
    List<LineItem> lines) {

  public record LineItem(String productName, int quantity, BigDecimal unitPriceAmount) {}
}
