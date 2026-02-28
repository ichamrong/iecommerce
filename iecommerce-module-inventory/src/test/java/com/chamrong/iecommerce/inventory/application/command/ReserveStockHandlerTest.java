package com.chamrong.iecommerce.inventory.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.inventory.application.dto.ReserveStockRequest;
import com.chamrong.iecommerce.inventory.domain.ClockPort;
import com.chamrong.iecommerce.inventory.domain.IdempotencyPort;
import com.chamrong.iecommerce.inventory.domain.InventoryItem;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.OutOfStockException;
import com.chamrong.iecommerce.inventory.domain.ReservationPort;
import com.chamrong.iecommerce.inventory.domain.StockReservation;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ReserveStockHandler}.
 *
 * <p>All DB and Redis dependencies are mocked. {@code init()} is called manually because
 * {@code @PostConstruct} is NOT invoked without a Spring context.
 */
@ExtendWith(MockitoExtension.class)
class ReserveStockHandlerTest {

  @Mock private OnHandProjectionPort projection;
  @Mock private ReservationPort reservations;
  @Mock private LedgerPort ledger;
  @Mock private IdempotencyPort idempotency;
  @Mock private ClockPort clock;

  private ReserveStockHandler handler;

  private static final String TENANT_ID = "tenant-1";
  private static final Long PRODUCT_ID = 100L;
  private static final Long WH_ID = 200L;
  private static final String REF_ID = "ORDER-001";
  private static final Instant NOW = Instant.parse("2025-03-01T12:00:00Z");

  @BeforeEach
  void setUp() {
    handler =
        new ReserveStockHandler(
            projection, reservations, ledger, idempotency, clock, new CompositeMeterRegistry());
    handler.init(); // @PostConstruct is not called by Mockito — invoke manually
    // lenient: clock.now() is NOT called in the early-return (duplicate) path
    org.mockito.Mockito.lenient().when(clock.now()).thenReturn(NOW);
  }

  private InventoryItem itemWith(int onHand) {
    var item = InventoryItem.create(TENANT_ID, PRODUCT_ID, WH_ID);
    item.applyReceipt(onHand);
    return item;
  }

  private ReserveStockRequest req(int qty) {
    return new ReserveStockRequest(
        TENANT_ID, PRODUCT_ID, WH_ID, qty, "ORDER", REF_ID, "user1", NOW.plusSeconds(3600));
  }

  @Test
  void handle_success_createsReservationAndAppendsLedger() {
    var item = itemWith(50);
    // Non-duplicate path: no existing reservation
    when(reservations.findByRef(TENANT_ID, "ORDER", REF_ID)).thenReturn(Optional.empty());
    when(projection.findForUpdate(TENANT_ID, PRODUCT_ID, WH_ID)).thenReturn(Optional.of(item));
    when(reservations.save(any())).thenAnswer(inv -> inv.getArgument(0));

    handler.handle(req(10));

    assertThat(item.getReservedQty()).isEqualTo(10);
    assertThat(item.getAvailableQty()).isEqualTo(40);
    verify(projection).save(item);
    verify(reservations).save(any(StockReservation.class));
    verify(ledger).append(any());
    verify(idempotency).record(TENANT_ID, "RESERVE", REF_ID, "");
  }

  @Test
  void handle_duplicate_referenceId_returnsExistingWithoutRepeat() {
    var existing =
        StockReservation.create(
            TENANT_ID, PRODUCT_ID, WH_ID, 10, "ORDER", REF_ID, NOW.plusSeconds(3600), NOW);
    // Duplicate path: existing reservation found immediately
    when(reservations.findByRef(TENANT_ID, "ORDER", REF_ID)).thenReturn(Optional.of(existing));

    var result = handler.handle(req(10));

    assertThat(result).isSameAs(existing);
    verify(projection, never()).save(any());
    verify(ledger, never()).append(any());
    verify(idempotency, never()).record(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void handle_outOfStock_throwsOutOfStockException() {
    var item = itemWith(5); // only 5 available
    when(reservations.findByRef(TENANT_ID, "ORDER", REF_ID)).thenReturn(Optional.empty());
    when(projection.findForUpdate(TENANT_ID, PRODUCT_ID, WH_ID)).thenReturn(Optional.of(item));

    assertThatThrownBy(() -> handler.handle(req(10))) // request 10 but only 5 available
        .isInstanceOf(OutOfStockException.class)
        .hasMessageContaining("Insufficient stock");

    verify(projection, never()).save(any());
    verify(reservations, never()).save(any());
  }

  @Test
  void handle_projectionNotFound_throwsOutOfStock() {
    when(reservations.findByRef(TENANT_ID, "ORDER", REF_ID)).thenReturn(Optional.empty());
    when(projection.findForUpdate(TENANT_ID, PRODUCT_ID, WH_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(req(5))).isInstanceOf(OutOfStockException.class);
  }
}
