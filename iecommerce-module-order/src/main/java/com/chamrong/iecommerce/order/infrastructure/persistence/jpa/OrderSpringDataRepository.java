package com.chamrong.iecommerce.order.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSpringDataRepository extends JpaRepository<OrderEntity, Long> {

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
  List<OrderEntity> findByTenantIdAndCreatedAtBetween(String tenantId, Instant start, Instant end);
}
