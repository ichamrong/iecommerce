package com.chamrong.iecommerce.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link StockReservation} FSM invariants. */
class StockReservationTest {

  private static final Instant NOW = Instant.parse("2025-03-01T12:00:00Z");

  private StockReservation pending(int qty) {
    return StockReservation.create(
        "t1", 1L, 1L, qty, "ORDER", "ref-001", NOW.plusSeconds(3600), NOW);
  }

  @Test
  void newReservation_isPending() {
    var r = pending(10);
    assertThat(r.getStatus()).isEqualTo(StockReservation.ReservationStatus.PENDING);
    assertThat(r.isPending()).isTrue();
  }

  @Test
  void commit_pendingReservation_becomesCommitted() {
    var r = pending(10);
    r.commit(NOW.plusSeconds(10));
    assertThat(r.getStatus()).isEqualTo(StockReservation.ReservationStatus.COMMITTED);
  }

  @Test
  void release_pendingReservation_becomesReleased() {
    var r = pending(10);
    r.release(NOW.plusSeconds(5));
    assertThat(r.getStatus()).isEqualTo(StockReservation.ReservationStatus.RELEASED);
  }

  @Test
  void expire_pendingReservation_becomesExpired() {
    var r = pending(10);
    r.expire(NOW.plusSeconds(3700));
    assertThat(r.getStatus()).isEqualTo(StockReservation.ReservationStatus.EXPIRED);
  }

  @Test
  void commit_alreadyCommitted_throws() {
    var r = pending(10);
    r.commit(NOW.plusSeconds(1));
    assertThatThrownBy(() -> r.commit(NOW.plusSeconds(2)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("COMMITTED");
  }

  @Test
  void release_alreadyReleased_throws() {
    var r = pending(10);
    r.release(NOW.plusSeconds(1));
    assertThatThrownBy(() -> r.release(NOW.plusSeconds(2)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("RELEASED");
  }

  @Test
  void expire_alreadyCommitted_throws() {
    var r = pending(10);
    r.commit(NOW.plusSeconds(1));
    assertThatThrownBy(() -> r.expire(NOW.plusSeconds(2)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("COMMITTED");
  }
}
