package com.chamrong.iecommerce.inventory.application.query;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.inventory.application.dto.LedgerEntryResponse;
import com.chamrong.iecommerce.inventory.application.dto.OnHandResponse;
import com.chamrong.iecommerce.inventory.application.dto.ReservationResponse;
import com.chamrong.iecommerce.inventory.domain.InventoryItem;
import com.chamrong.iecommerce.inventory.domain.LedgerPort;
import com.chamrong.iecommerce.inventory.domain.OnHandProjectionPort;
import com.chamrong.iecommerce.inventory.domain.ReservationPort;
import com.chamrong.iecommerce.inventory.domain.StockLedgerEntry;
import com.chamrong.iecommerce.inventory.domain.StockReservation;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side queries for inventory.
 *
 * <p>All methods are read-only transactions. Hot paths ({@code getOnHand}) rely on a Redis cache
 * layer ({@link InventoryCachePort}) with a DB fallback — see infrastructure layer for cache
 * implementation.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryQueryHandler {

  static final int DEFAULT_LIMIT = 20;
  static final int MAX_LIMIT = 100;

  /** Endpoint keys for filter hashing. */
  static final String ENDPOINT_LEDGER = "inventory:ledger";

  static final String ENDPOINT_RESERVATIONS = "inventory:reservations";

  private final OnHandProjectionPort projection;
  private final LedgerPort ledger;
  private final ReservationPort reservations;

  // ── On-Hand ──────────────────────────────────────────────────────────────

  /** Returns on-hand stock for the specified product, across all warehouses. */
  public List<OnHandResponse> getOnHand(String tenantId, Long productId) {
    return projection.findAllByProduct(tenantId, productId).stream()
        .map(this::toOnHandResponse)
        .toList();
  }

  /** Returns on-hand stock for a specific product–warehouse pair. */
  public Optional<OnHandResponse> getOnHandByWarehouse(
      String tenantId, Long productId, Long warehouseId) {
    return projection.find(tenantId, productId, warehouseId).map(this::toOnHandResponse);
  }

  // ── Ledger History ───────────────────────────────────────────────────────

  /**
   * Cursor-paginated ledger history sorted by {@code (created_at DESC, id DESC)}.
   *
   * @param tenantId required tenant scope
   * @param productId product filter
   * @param warehouseId optional warehouse filter; null = all warehouses
   * @param cursor opaque cursor; null = first page
   * @param limit page size (capped at {@value #MAX_LIMIT})
   */
  public CursorPageResponse<LedgerEntryResponse> getLedgerHistory(
      String tenantId, Long productId, Long warehouseId, String cursor, int limit) {

    int pageSize = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
    int fetchSize = pageSize + 1;

    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);
    filterMap.put("productId", productId);
    filterMap.put("warehouseId", warehouseId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LEDGER, filterMap);

    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<StockLedgerEntry> rows =
        ledger.findPage(tenantId, productId, warehouseId, cursorCreatedAt, cursorId, fetchSize);

    boolean hasNext = rows.size() == fetchSize;
    List<StockLedgerEntry> page = hasNext ? rows.subList(0, pageSize) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      var last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    List<LedgerEntryResponse> data = page.stream().map(this::toLedgerResponse).toList();
    return CursorPageResponse.of(data, nextCursor, hasNext, pageSize);
  }

  // ── Reservations ─────────────────────────────────────────────────────────

  /**
   * Cursor-paginated reservation list for a product.
   *
   * @param tenantId required tenant scope
   * @param productId product filter
   * @param status optional status filter; null = all statuses
   * @param cursor opaque cursor; null = first page
   * @param limit page size
   */
  public CursorPageResponse<ReservationResponse> getReservations(
      String tenantId,
      Long productId,
      StockReservation.ReservationStatus status,
      String cursor,
      int limit) {

    int pageSize = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);
    int fetchSize = pageSize + 1;

    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);
    filterMap.put("productId", productId);
    filterMap.put("status", status != null ? status.name() : null);
    String filterHash = FilterHasher.computeHash(ENDPOINT_RESERVATIONS, filterMap);

    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<StockReservation> rows =
        reservations.findPage(tenantId, productId, status, cursorCreatedAt, cursorId, fetchSize);

    boolean hasNext = rows.size() == fetchSize;
    List<StockReservation> page = hasNext ? rows.subList(0, pageSize) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      var last = page.get(page.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }

    List<ReservationResponse> data = page.stream().map(this::toReservationResponse).toList();
    return CursorPageResponse.of(data, nextCursor, hasNext, pageSize);
  }

  // ── Mappers ───────────────────────────────────────────────────────────────

  private OnHandResponse toOnHandResponse(InventoryItem item) {
    return new OnHandResponse(
        item.getProductId(),
        item.getWarehouseId(),
        item.getOnHandQty(),
        item.getReservedQty(),
        item.getAvailableQty());
  }

  private LedgerEntryResponse toLedgerResponse(StockLedgerEntry e) {
    return new LedgerEntryResponse(
        e.getId(),
        e.getProductId(),
        e.getWarehouseId(),
        e.getEntryType(),
        e.getQtyDelta(),
        e.getReferenceType(),
        e.getReferenceId(),
        e.getActorId(),
        e.getCreatedAt());
  }

  private ReservationResponse toReservationResponse(StockReservation r) {
    return new ReservationResponse(
        r.getId(),
        r.getProductId(),
        r.getWarehouseId(),
        r.getQty(),
        r.getReferenceType(),
        r.getReferenceId(),
        r.getStatus(),
        r.getExpiresAt(),
        r.getCreatedAt());
  }
}
