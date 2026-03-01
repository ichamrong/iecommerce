package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.OrderItem;
import com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderEntity;
import com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderItemEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps between Order/OrderItem (domain) and OrderEntity/OrderItemEntity (persistence). */
@Component
public class OrderPersistenceMapper {

  /** Converts persistence entity to domain aggregate (for reads). */
  public Order toDomain(OrderEntity entity) {
    if (entity == null) {
      return null;
    }
    Order order = new Order();
    order.setId(entity.getId());
    order.setTenantId(entity.getTenantId());
    order.setCreatedAt(entity.getCreatedAt());
    order.setUpdatedAt(entity.getUpdatedAt());
    order.setDeleted(entity.isDeleted());
    order.setDeletedAt(entity.getDeletedAt());
    order.setVersion(entity.getVersion());
    if (entity.getCode() != null) {
      order.assignCode(entity.getCode());
    }
    order.setCustomerId(entity.getCustomerId());
    order.setState(entity.getState());
    order.setShippingAddress(entity.getShippingAddress());
    order.setConfirmedAt(entity.getConfirmedAt());
    order.setCancelledAt(entity.getCancelledAt());
    order.setShippedAt(entity.getShippedAt());
    order.setTrackingNumber(entity.getTrackingNumber());
    order.setVoucherCode(entity.getVoucherCode());
    order.setDiscount(entity.getDiscount());
    order.setSubTotal(entity.getSubTotal());
    order.setTotal(entity.getTotal());
    order.setDepositAmount(entity.getDepositAmount());
    List<OrderItem> items =
        entity.getItems() == null
            ? new ArrayList<>()
            : entity.getItems().stream().map(this::toDomainItem).collect(Collectors.toList());
    order.restoreItems(items);
    return order;
  }

  /** Converts domain aggregate to persistence entity (for save). */
  public OrderEntity toEntity(Order order) {
    if (order == null) {
      return null;
    }
    OrderEntity entity = new OrderEntity();
    if (order.getId() != null) {
      entity.setId(order.getId());
    }
    entity.setTenantId(order.getTenantId());
    entity.setVersion(order.getVersion() != null ? order.getVersion() : 0L);
    entity.setCode(order.getCode());
    entity.setCustomerId(order.getCustomerId());
    entity.setState(order.getState());
    entity.setShippingAddress(order.getShippingAddress());
    entity.setConfirmedAt(order.getConfirmedAt());
    entity.setCancelledAt(order.getCancelledAt());
    entity.setShippedAt(order.getShippedAt());
    entity.setTrackingNumber(order.getTrackingNumber());
    entity.setVoucherCode(order.getVoucherCode());
    entity.setDiscount(order.getDiscount());
    entity.setSubTotal(order.getSubTotal());
    entity.setTotal(order.getTotal());
    entity.setDepositAmount(order.getDepositAmount());
    List<OrderItemEntity> itemEntities = new ArrayList<>();
    if (order.getItems() != null) {
      for (OrderItem item : order.getItems()) {
        OrderItemEntity itemEntity = toEntityItem(item, entity);
        itemEntities.add(itemEntity);
      }
    }
    entity.getItems().clear();
    entity.getItems().addAll(itemEntities);
    return entity;
  }

  public OrderItem toDomainItem(OrderItemEntity entity) {
    if (entity == null) {
      return null;
    }
    OrderItem item =
        OrderItem.of(
            entity.getProductVariantId(),
            entity.getQuantity(),
            entity.getUnitPrice(),
            entity.getStartAt(),
            entity.getEndAt());
    item.setId(entity.getId());
    item.setCreatedAt(entity.getCreatedAt());
    item.setUpdatedAt(entity.getUpdatedAt());
    return item;
  }

  public OrderItemEntity toEntityItem(OrderItem item, OrderEntity orderEntity) {
    if (item == null) {
      return null;
    }
    OrderItemEntity entity = new OrderItemEntity();
    if (item.getId() != null) {
      entity.setId(item.getId());
    }
    entity.setOrder(orderEntity);
    entity.setProductVariantId(item.getProductVariantId());
    entity.setQuantity(item.getQuantity());
    entity.setUnitPrice(item.getUnitPrice());
    entity.setStartAt(item.getStartAt());
    entity.setEndAt(item.getEndAt());
    return entity;
  }
}
