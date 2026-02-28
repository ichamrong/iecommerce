package com.chamrong.iecommerce.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InventoryItem} domain invariants. Pure Java — no Spring context required.
 */
class InventoryItemTest {

  private InventoryItem item(int onHand) {
    var i = InventoryItem.create("t1", 1L, 1L);
    i.applyReceipt(onHand);
    return i;
  }

  @Test
  void applyReceipt_incrementsOnHand() {
    var i = item(50);
    i.applyReceipt(20);
    assertThat(i.getOnHandQty()).isEqualTo(70);
    assertThat(i.getAvailableQty()).isEqualTo(70);
  }

  @Test
  void applyReceipt_zeroQty_throws() {
    var i = item(50);
    assertThatThrownBy(() -> i.applyReceipt(0)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void incrementReserved_reducesAvailable() {
    var i = item(50);
    i.incrementReserved(15);
    assertThat(i.getOnHandQty()).isEqualTo(50);
    assertThat(i.getReservedQty()).isEqualTo(15);
    assertThat(i.getAvailableQty()).isEqualTo(35);
  }

  @Test
  void incrementReserved_insufficientStock_throwsOutOfStock() {
    var i = item(10);
    assertThatThrownBy(() -> i.incrementReserved(11))
        .isInstanceOf(OutOfStockException.class)
        .hasMessageContaining("Insufficient stock");
  }

  @Test
  void incrementReserved_exactlyAvailable_succeeds() {
    var i = item(10);
    i.incrementReserved(10);
    assertThat(i.getAvailableQty()).isEqualTo(0);
    assertThat(i.getReservedQty()).isEqualTo(10);
  }

  @Test
  void decrementReserved_restoresAvailable() {
    var i = item(50);
    i.incrementReserved(20);
    i.decrementReserved(10);
    assertThat(i.getReservedQty()).isEqualTo(10);
    assertThat(i.getAvailableQty()).isEqualTo(40);
  }

  @Test
  void decrementReserved_moreThanReserved_throws() {
    var i = item(50);
    i.incrementReserved(5);
    assertThatThrownBy(() -> i.decrementReserved(6))
        .isInstanceOf(InsufficientReservationException.class);
  }

  @Test
  void commitReservation_deductsBothOnHandAndReserved() {
    var i = item(100);
    i.incrementReserved(30);
    i.commitReservation(30);
    assertThat(i.getOnHandQty()).isEqualTo(70);
    assertThat(i.getReservedQty()).isEqualTo(0);
    assertThat(i.getAvailableQty()).isEqualTo(70);
  }

  @Test
  void commitReservation_moreThanReserved_throws() {
    var i = item(100);
    i.incrementReserved(10);
    assertThatThrownBy(() -> i.commitReservation(11))
        .isInstanceOf(InsufficientReservationException.class);
  }

  @Test
  void applyAdjustment_negative_deductsOnHand() {
    var i = item(100);
    i.applyAdjustment(-10, false);
    assertThat(i.getOnHandQty()).isEqualTo(90);
  }

  @Test
  void applyAdjustment_negativeBeyondZero_throwsWhenNotAllowed() {
    var i = item(5);
    assertThatThrownBy(() -> i.applyAdjustment(-10, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("negative");
  }

  @Test
  void applyAdjustment_negativeBeyondZero_allowedWhenForced() {
    var i = item(5);
    i.applyAdjustment(-10, true);
    assertThat(i.getOnHandQty()).isEqualTo(-5);
  }

  @Test
  void backwardCompatAliases_returnSameValues() {
    var i = item(50);
    i.incrementReserved(10);
    assertThat(i.getQuantity()).isEqualTo(50);
    assertThat(i.getReservedQuantity()).isEqualTo(10);
    assertThat(i.getAvailableQuantity()).isEqualTo(40);
  }
}
