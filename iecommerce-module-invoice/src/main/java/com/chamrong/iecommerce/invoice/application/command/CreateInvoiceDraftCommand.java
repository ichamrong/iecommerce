package com.chamrong.iecommerce.invoice.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Command: create a new DRAFT invoice.
 *
 * @param tenantId owning tenant (extracted from JWT — not user-supplied)
 * @param actorId authenticated user ID
 * @param orderId optional linked order
 * @param customerId optional customer
 * @param currency ISO 4217 currency code
 * @param dueDate payment due date
 * @param sellerSnapshot JSON of seller info at this moment
 * @param buyerSnapshot JSON of buyer info at this moment
 * @param lines initial line items (may be empty; lines can be added later)
 */
public record CreateInvoiceDraftCommand(
    String tenantId,
    String actorId,
    Long orderId,
    Long customerId,
    String currency,
    LocalDate dueDate,
    String sellerSnapshot,
    String buyerSnapshot,
    List<LineItem> lines) {

  /**
   * @param sku optional SKU
   * @param productName display name
   * @param description optional description
   * @param quantity must be >= 1
   * @param unitPriceAmount monetary amount
   * @param taxRate decimal fraction (e.g. 0.10 for 10%)
   * @param lineOrder 0-based display order
   */
  public record LineItem(
      String sku,
      String productName,
      String description,
      int quantity,
      BigDecimal unitPriceAmount,
      BigDecimal taxRate,
      int lineOrder) {}
}
