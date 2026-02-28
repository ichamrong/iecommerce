package com.chamrong.iecommerce.inventory.domain;

/**
 * Thrown when a release/commit operation specifies more units than currently reserved.
 *
 * <p>Mapped to HTTP 409 Conflict by the global exception handler.
 */
public class InsufficientReservationException extends RuntimeException {

  public InsufficientReservationException(Long productId, int requested, int currentReserved) {
    super(
        "Insufficient reservation for product "
            + productId
            + ": requested="
            + requested
            + " currentReserved="
            + currentReserved);
  }
}
