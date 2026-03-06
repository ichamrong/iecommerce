package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Keyset-paginated JPA implementation for Order repository. Maps between Order (domain) and
 * OrderEntity.
 */
@Component
@RequiredArgsConstructor
public class JpaOrderAdapter implements OrderRepositoryPort {

  private final OrderSpringDataRepository repository;
  private final OrderPersistenceMapper mapper;

  @Override
  public Optional<Order> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Order> findByIdForUpdate(Long id) {
    return repository.findByIdForUpdate(id).map(mapper::toDomain);
  }

  @Override
  public Order save(Order order) {
    OrderEntity entity = mapper.toEntity(order);
    OrderEntity saved = repository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public List<Order> findByCustomerFirstPage(String tenantId, Long customerId, int limit) {
    return repository.findFirstPage(tenantId, customerId, PageRequest.of(0, limit)).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByCustomerNextPage(
      String tenantId, Long customerId, Instant createdAt, Long id, int limit) {
    return repository
        .findNextPage(tenantId, customerId, createdAt, id, PageRequest.of(0, limit))
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByStateFirstPage(
      String tenantId, com.chamrong.iecommerce.order.domain.OrderState state, int limit) {
    return repository.findByStateFirstPage(tenantId, state, PageRequest.of(0, limit)).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByStateNextPage(
      String tenantId,
      com.chamrong.iecommerce.order.domain.OrderState state,
      Instant createdAt,
      Long id,
      int limit) {
    return repository
        .findByStateNextPage(tenantId, state, createdAt, id, PageRequest.of(0, limit))
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByTenantFirstPage(String tenantId, int limit) {
    return repository.findByTenantFirstPage(tenantId, PageRequest.of(0, limit)).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByTenantNextPage(String tenantId, Instant createdAt, Long id, int limit) {
    return repository
        .findByTenantNextPage(tenantId, createdAt, id, PageRequest.of(0, limit))
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findByTenantIdAndCreatedAtBetween(
      String tenantId, Instant start, Instant end) {
    return repository.findByTenantIdAndCreatedAtBetween(tenantId, start, end).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}
