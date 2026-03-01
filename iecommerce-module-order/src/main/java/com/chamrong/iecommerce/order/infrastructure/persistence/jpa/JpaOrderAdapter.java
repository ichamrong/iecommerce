package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.domain.Order;
import com.chamrong.iecommerce.order.domain.ports.OrderRepositoryPort;
import com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

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

  @Repository
  interface OrderSpringDataRepository extends JpaRepository<OrderEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
    Optional<OrderEntity> findByIdForUpdate(Long id);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId AND o.customerId = :customerId"
            + " ORDER BY o.createdAt DESC, o.id DESC")
    List<OrderEntity> findFirstPage(String tenantId, Long customerId, Pageable pageable);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId AND o.customerId = :customerId "
            + "AND (o.createdAt < :createdAt OR (o.createdAt = :createdAt AND o.id < :id)) "
            + "ORDER BY o.createdAt DESC, o.id DESC")
    List<OrderEntity> findNextPage(
        String tenantId, Long customerId, Instant createdAt, Long id, Pageable pageable);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId AND o.state = :state"
            + " ORDER BY o.createdAt DESC, o.id DESC")
    List<OrderEntity> findByStateFirstPage(
        String tenantId, com.chamrong.iecommerce.order.domain.OrderState state, Pageable pageable);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId AND o.state = :state "
            + "AND (o.createdAt < :createdAt OR (o.createdAt = :createdAt AND o.id < :id)) "
            + "ORDER BY o.createdAt DESC, o.id DESC")
    List<OrderEntity> findByStateNextPage(
        String tenantId,
        com.chamrong.iecommerce.order.domain.OrderState state,
        Instant createdAt,
        Long id,
        Pageable pageable);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId"
            + " ORDER BY o.createdAt DESC, o.id DESC")
    List<OrderEntity> findByTenantFirstPage(String tenantId, Pageable pageable);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId "
            + "AND (o.createdAt < :createdAt OR (o.createdAt = :createdAt AND o.id < :id)) "
            + "ORDER BY o.createdAt DESC, o.id DESC")
    List<OrderEntity> findByTenantNextPage(
        String tenantId, Instant createdAt, Long id, Pageable pageable);

    @Query(
        "SELECT o FROM OrderEntity o WHERE o.tenantId = :tenantId AND o.createdAt >= :start AND"
            + " o.createdAt <= :end ORDER BY o.createdAt ASC, o.id ASC")
    List<OrderEntity> findByTenantIdAndCreatedAtBetween(
        String tenantId, Instant start, Instant end);
  }
}
