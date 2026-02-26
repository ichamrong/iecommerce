package com.chamrong.iecommerce.inventory.domain;

/**
 * Domain exception thrown when inventory operations fail due to insufficient stock.
 *
 * <p>This replaces generic IllegalStateExceptions to allow specific HTTP 409 Conflict mappings or
 * distinct Saga compensation routing.
 */
public class OutOfStockException extends RuntimeException {

  public OutOfStockException(String message) {
    super(message);
  }

  public OutOfStockException(Long productId) {
    super(String.format("Insufficient stock for product ID: %d", productId));
  }
}
