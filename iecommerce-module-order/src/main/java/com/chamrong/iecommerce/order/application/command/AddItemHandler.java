package com.chamrong.iecommerce.order.application.command;

import com.chamrong.iecommerce.catalog.CatalogApi;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.order.application.dto.AddItemRequest;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.order.application.dto.OrderResponse.OrderItemResponse;
import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderAuditActions;
import com.chamrong.iecommerce.order.domain.OrderItem;
import com.chamrong.iecommerce.order.domain.OrderState;
import com.chamrong.iecommerce.order.domain.ports.OrderAuditPort;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: Add an item to an existing draft order. Validates against catalog and recalculates
 * totals.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddItemHandler {

  private final OrderRepositoryPort orderRepository;
  private final CatalogApi catalogApi;
  private final OrderAuditPort auditPort;
  private final MeterRegistry metrics;

  @Transactional
  public OrderResponse handle(Long orderId, AddItemRequest req, String actor) {
    Objects.requireNonNull(orderId, "orderId must not be null");
    Objects.requireNonNull(req, "AddItemRequest must not be null");

    final String tenantId = TenantContext.requireTenantId();

    // Load order with pessimistic lock for update
    final Order order =
        orderRepository
            .findByIdForUpdate(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

    // Security check: tenant must match
    if (!order.getTenantId().equals(tenantId)) {
      throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }

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
      item.updateQuantity(newQty);
      log.debug(
          "Merged quantity for variant {} in order {}. New qty: {}",
          req.productVariantId(),
          orderId,
          newQty);
    } else {
      final OrderItem item =
          OrderItem.of(
              req.productVariantId(),
              req.quantity(),
              new Money(catalogVariant.priceAmount(), catalogVariant.priceCurrency()),
              req.startAt(),
              req.endAt());
      order.addItem(item);
      log.debug("Added new item variant {} to order {}", req.productVariantId(), orderId);
    }

    order.recalculateTotals();
    final Order saved = orderRepository.save(order);

    auditPort.log(
        saved.getId(),
        tenantId,
        OrderState.AddingItems,
        OrderState.AddingItems,
        OrderAuditActions.ITEM_ADDED,
        actor,
        "variantId=" + req.productVariantId() + " qty=" + req.quantity());

    metrics.counter("order.item.added", "tenant", tenantId).increment();

    return toResponse(saved);
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
