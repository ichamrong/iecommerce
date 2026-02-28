package com.chamrong.iecommerce.inventory.domain;

/**
 * Thrown when available stock quantity is insufficient to satisfy a reservation or sale.
 *
 * <p>Mapped to HTTP 409 Conflict by the global exception handler.
 */
public class OutOfStockException extends RuntimeException {

  private final Long productId;

  // Keep original constructor for backward compat with existing code
  public OutOfStockException(Long productId) {
    super("Out of stock for product: " + productId);
    this.productId = productId;
  }

  public OutOfStockException(String message) {
    super(message);
    this.productId = null;
  }

  public OutOfStockException(Long productId, int requested, int available) {
    super(
        "Insufficient stock for product "
            + productId
            + ": requested="
            + requested
            + " available="
            + available);
    this.productId = productId;
  }

  public Long getProductId() {
    return productId;
  }
}
