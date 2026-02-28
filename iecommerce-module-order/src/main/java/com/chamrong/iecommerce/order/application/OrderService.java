package com.chamrong.iecommerce.order.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.event.OrderCancelledEvent;
import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.common.event.OrderConfirmedEvent;
import com.chamrong.iecommerce.common.event.OrderShippedEvent;
import com.chamrong.iecommerce.order.OrderApi;
import com.chamrong.iecommerce.order.application.dto.AddItemRequest;
import com.chamrong.iecommerce.order.application.dto.CreatePosOrderRequest;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderAuditLog;
import com.chamrong.iecommerce.order.domain.OrderAuditLogRepository;
import com.chamrong.iecommerce.order.domain.OrderItem;
import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import com.chamrong.iecommerce.order.domain.OrderOutboxRepository;
import com.chamrong.iecommerce.order.domain.OrderRepository;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.promotion.PromotionApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderApi {

  private final OrderRepository orderRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final PromotionApi promotionApi;
  private final com.chamrong.iecommerce.catalog.CatalogApi catalogApi;
  private final OrderAuditLogRepository auditLogRepository;
  private final OrderOutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  // ── Commands ───────────────────────────────────────────────────────────────

  // Called by REST layer (returns DTO)
  @Transactional
  public OrderResponse createDraftOrder() {
    final String tenantId = com.chamrong.iecommerce.common.TenantContext.requireTenantId();
    final Order order = new Order();
    order.assignCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    order.assignTenantId(tenantId);
    final Order saved = orderRepository.save(order);
    log.info("Created draft order id={} code={}", saved.getId(), saved.getCode());
    audit(
        saved,
        null,
        OrderState.AddingItems,
        OrderAuditActions.ORDER_CREATED,
        "tenantId=" + tenantId + " code=" + saved.getCode());
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse addItem(final Long orderId, final AddItemRequest req) {
    Objects.requireNonNull(orderId, "orderId must not be null");
    Objects.requireNonNull(req, "AddItemRequest must not be null");

    final Order order = requireOrder(orderId);
    if (order.getState() != OrderState.AddingItems) {
      throw new IllegalStateException("Cannot add items to an order in state: " + order.getState());
    }

    final var catalogVariant =
        catalogApi
            .findActiveVariant(req.productVariantId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Invalid or inactive product variant: " + req.productVariantId()));

    // Search for existing line item for this variant — merge quantity if found
    final var existing =
        order.getItems().stream()
            .filter(i -> i.getProductVariantId().equals(req.productVariantId()))
            .findFirst();

    if (existing.isPresent()) {
      final OrderItem item = existing.get();
      final int newQty = item.getQuantity() + req.quantity();
      if (newQty > Order.MAX_QUANTITY) {
        throw new IllegalArgumentException(
            "Combined quantity " + newQty + " exceeds maximum " + Order.MAX_QUANTITY);
      }
      item.setQuantity(newQty);
    } else {
      final OrderItem item = new OrderItem();
      item.setProductVariantId(req.productVariantId());
      item.setQuantity(req.quantity());
      // Always bind catalog price — never trust client-submitted prices
      item.setUnitPrice(new Money(catalogVariant.priceAmount(), catalogVariant.priceCurrency()));
      item.setStartAt(req.startAt());
      item.setEndAt(req.endAt());
      order.addItem(item);
    }

    order.recalculateTotals();
    final Order saved = orderRepository.save(order);
    audit(
        saved,
        OrderState.AddingItems,
        OrderState.AddingItems,
        OrderAuditActions.ITEM_ADDED,
        "variantId=" + req.productVariantId());
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse applyVoucher(final Long orderId, final String code) {
    Objects.requireNonNull(orderId, "orderId must not be null");
    Objects.requireNonNull(code, "voucher code must not be null");

    final Order order = requireOrder(orderId);
    final Money discount =
        promotionApi
            .calculateDiscount(order.getTenantId(), code, order.getSubTotal())
            .orElseThrow(
                () -> new IllegalArgumentException("Invalid or expired voucher code: " + code));

    // applyVoucher enforces state + non-empty order guards internally
    order.applyVoucher(discount, code);
    final Order saved = orderRepository.save(order);
    audit(
        saved,
        OrderState.AddingItems,
        OrderState.AddingItems,
        OrderAuditActions.VOUCHER_APPLIED,
        "code=" + code);
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse confirm(Long orderId) {
    Order order = requireOrder(orderId);
    OrderState prev = order.getState();
    order.confirm();

    Order saved = orderRepository.save(order);

    // Saga Event: Inventory will listen and reserve stock asynchronously
    var items =
        saved.getItems().stream()
            .map(i -> new OrderConfirmedEvent.Item(i.getProductVariantId(), i.getQuantity()))
            .toList();
    saveOutbox(
        saved.getTenantId(),
        "OrderConfirmedEvent",
        new OrderConfirmedEvent(saved.getId(), saved.getTenantId(), saved.getCustomerId(), items));

    audit(
        saved,
        prev,
        OrderState.Confirmed,
        OrderAuditActions.ORDER_CONFIRMED,
        "items=" + saved.getItems().size());
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse pick(Long orderId) {
    Order order = requireOrder(orderId);
    OrderState prev = order.getState();
    order.pick();
    Order saved = orderRepository.save(order);
    audit(saved, prev, OrderState.Picking, OrderAuditActions.ORDER_PICKING, null);
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse pack(Long orderId) {
    Order order = requireOrder(orderId);
    OrderState prev = order.getState();
    order.pack();
    Order saved = orderRepository.save(order);
    audit(saved, prev, OrderState.Packing, OrderAuditActions.ORDER_PACKING, null);
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse ship(final Long orderId, final String trackingNumber) {
    final Order order = requireOrder(orderId);
    final OrderState prev = order.getState();
    // Tracking validation now enforced inside the domain method itself
    order.ship(trackingNumber);

    final Order saved = orderRepository.save(order);

    // Saga Event: Inventory will listen and deduct stock permanently
    var items =
        saved.getItems().stream()
            .map(i -> new OrderShippedEvent.Item(i.getProductVariantId(), i.getQuantity()))
            .toList();
    saveOutbox(
        saved.getTenantId(),
        "OrderShippedEvent",
        new OrderShippedEvent(
            saved.getId(), saved.getTenantId(), saved.getCustomerId(), trackingNumber, items));

    audit(
        saved,
        prev,
        OrderState.Shipped,
        OrderAuditActions.ORDER_SHIPPED,
        "tracking=" + trackingNumber);
    return toResponse(saved);
  }

  @Override
  @Transactional
  public OrderResponse createPosOrder(String tenantId, CreatePosOrderRequest req) {
    Order order = new Order();
    order.assignTenantId(tenantId);
    order.setCustomerId(req.customerId());

    for (CreatePosOrderRequest.Item itemReq : req.items()) {
      OrderItem item = new OrderItem();
      item.setProductVariantId(itemReq.productVariantId());
      item.setQuantity(itemReq.quantity());
      item.setUnitPrice(new Money(itemReq.price(), req.currency()));
      order.addItem(item);
    }

    order.setTotalManual(new Money(req.amountPaid(), req.currency()));

    // Transition to confirmed
    order.confirm();

    // Transition to payment settled (since it's POS, paid upfront)
    order.pay();

    // Transition to completed immediately (Bypass pick/pack/ship)
    order.completeImmediate();

    Order saved = orderRepository.save(order);

    // Publish stock deduction event (immediate inventory relief)
    var items =
        saved.getItems().stream()
            .map(i -> new OrderShippedEvent.Item(i.getProductVariantId(), i.getQuantity()))
            .toList();

    saveOutbox(
        saved.getTenantId(),
        com.chamrong.iecommerce.common.event.EventConstants
            .ORDER_SHIPPED, // Using ShippedEvent to trigger stock deduction in inventory module
        new OrderShippedEvent(
            saved.getId(), saved.getTenantId(), saved.getCustomerId(), "POS-HANDOVER", items));

    // Publish completion event for loyalty points
    int points = saved.getTotal() != null ? saved.getTotal().getAmount().intValue() : 0;
    saveOutbox(
        saved.getTenantId(),
        com.chamrong.iecommerce.common.event.EventConstants.ORDER_COMPLETED,
        new OrderCompletedEvent(saved.getTenantId(), saved.getId(), saved.getCustomerId(), points));

    audit(saved, null, OrderState.Completed, OrderAuditActions.ORDER_COMPLETED, "POS Sale");

    log.info(
        "POS Order id={} created and completed immediately for customer {}",
        saved.getId(),
        req.customerId());
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse deliver(Long orderId) {
    Order order = requireOrder(orderId);
    OrderState prev = order.getState();
    order.deliver();
    Order saved = orderRepository.save(order);
    audit(saved, prev, OrderState.Delivered, OrderAuditActions.ORDER_DELIVERED, null);
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse complete(Long orderId) {
    Order order = requireOrder(orderId);
    OrderState prev = order.getState();
    order.complete();
    Order saved = orderRepository.save(order);

    int points = saved.getTotal() != null ? saved.getTotal().getAmount().intValue() : 0;
    var completedEvent =
        new OrderCompletedEvent(saved.getTenantId(), saved.getId(), saved.getCustomerId(), points);

    // Use Outbox: write event to DB atomically with the order update.
    // The OutboxRelayScheduler will publish this after commit.
    saveOutbox(saved.getTenantId(), "OrderCompletedEvent", completedEvent);

    audit(
        saved,
        prev,
        OrderState.Completed,
        OrderAuditActions.ORDER_COMPLETED,
        "total="
            + (saved.getTotal() != null ? saved.getTotal().getAmount() : 0)
            + " points="
            + points);
    log.info("Order id={} completed, awarded {} loyalty points", saved.getId(), points);
    return toResponse(saved);
  }

  @Transactional
  public OrderResponse cancel(Long orderId) {
    Order order = requireOrder(orderId);
    OrderState oldState = order.getState();
    order.cancel();

    Order saved = orderRepository.save(order);

    // Saga Event: Release stock if it was previously reserved
    if (oldState == OrderState.Confirmed
        || oldState == OrderState.Picking
        || oldState == OrderState.Packing) {
      var items =
          saved.getItems().stream()
              .map(i -> new OrderCancelledEvent.Item(i.getProductVariantId(), i.getQuantity()))
              .toList();
      saveOutbox(
          saved.getTenantId(),
          "OrderCancelledEvent",
          new OrderCancelledEvent(
              saved.getId(), saved.getTenantId(), saved.getCustomerId(), items));
    }

    audit(
        saved,
        oldState,
        OrderState.Cancelled,
        OrderAuditActions.ORDER_CANCELLED,
        "cancelledFrom=" + oldState.name());
    return toResponse(saved);
  }

  // ── Queries ────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public Optional<OrderResponse> findById(Long id) {
    return orderRepository.findById(id).map(this::toResponse);
  }

  @Override
  @Transactional
  public Order createOrder(final String tenantId) {
    // Bridge for OrderApi interface consumers (other modules)
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    final Order o = new Order();
    o.assignCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    o.assignTenantId(tenantId);
    return orderRepository.save(o);
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.Optional<Order> getOrder(Long id) {
    return orderRepository.findById(id);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private Order requireOrder(Long id) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

    String currentTenant = com.chamrong.iecommerce.common.TenantContext.getCurrentTenant();
    if (currentTenant != null && !currentTenant.equals(order.getTenantId())) {
      log.warn(
          "IDOR attempt: Tenant {} tried to access order {} belonging to {}",
          currentTenant,
          id,
          order.getTenantId());
      // Audit even IDOR attempts so security teams can alert on them
      var idor =
          new OrderAuditLog(
              id,
              currentTenant,
              order.getState(),
              order.getState(),
              OrderAuditActions.IDOR_ATTEMPT,
              currentPrincipal(),
              "blockedTenant=" + order.getTenantId());
      auditLogRepository.save(idor);
      throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }
    return order;
  }

  /** Write an audit log entry. Called after every successful state transition. */
  private void audit(Order order, OrderState from, OrderState to, String action, String context) {
    var entry =
        new OrderAuditLog(
            order.getId(), order.getTenantId(), from, to, action, currentPrincipal(), context);
    auditLogRepository.save(entry);
  }

  /** Store an event in the Outbox table (same DB transaction as the Order save). */
  private void saveOutbox(String tenantId, String eventType, Object event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      outboxRepository.save(OrderOutboxEvent.pending(tenantId, eventType, payload));
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize outbox event type={}", eventType, e);
      throw new IllegalStateException("Cannot serialize event for outbox", e);
    }
  }

  /** Extract current user from Spring Security context. Falls back to 'system'. */
  private String currentPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
  }

  private OrderResponse toResponse(Order order) {
    List<OrderItemResponse> items =
        order.getItems().stream()
            .map(
                i ->
                    new OrderItemResponse(
                        i.getId(),
                        i.getProductVariantId(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getStartAt(),
                        i.getEndAt()))
            .toList();
    return new OrderResponse(
        order.getId(),
        order.getCode(),
        order.getCustomerId(),
        order.getState().name(),
        items,
        order.getSubTotal(),
        order.getTotal(),
        order.getShippingAddress(),
        order.getTrackingNumber(),
        order.getVoucherCode(),
        order.getDiscount(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }
}
