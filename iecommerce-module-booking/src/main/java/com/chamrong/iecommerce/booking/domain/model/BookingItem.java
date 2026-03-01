package com.chamrong.iecommerce.booking.domain.model;

import com.chamrong.iecommerce.common.Money;
import java.util.Objects;

/**
 * Line item in a booking: room, service, or add-on.
 *
 * @param resourceProductId product id
 * @param resourceVariantId optional variant
 * @param quantity         quantity
 * @param unitPrice        price per unit
 * @param totalPrice      line total
 * @param description      optional description
 */
public record BookingItem(
    Long resourceProductId,
    Long resourceVariantId,
    int quantity,
    Money unitPrice,
    Money totalPrice,
    String description) {

  public BookingItem {
    Objects.requireNonNull(resourceProductId, "resourceProductId");
    Objects.requireNonNull(unitPrice, "unitPrice");
    Objects.requireNonNull(totalPrice, "totalPrice");
    quantity = quantity > 0 ? quantity : 1;
    description = description != null ? description : "";
  }
}
