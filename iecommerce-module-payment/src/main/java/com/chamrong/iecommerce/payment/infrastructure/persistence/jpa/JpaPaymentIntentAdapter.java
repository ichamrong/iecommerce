package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.PaymentStatus;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@RequiredArgsConstructor
public class JpaPaymentIntentAdapter implements PaymentIntentRepositoryPort {

  private final PaymentIntentSpringRepository repository;

  @Override
  public void save(PaymentIntent intent) {
    PaymentIntentEntity entity = toEntity(intent);
    repository.save(entity);
  }

  @Override
  public Optional<PaymentIntent> findById(UUID intentId) {
    return repository.findById(intentId).map(this::toDomain);
  }

  @Override
  public Optional<PaymentIntent> findByIdempotencyKey(String tenantId, String idempotencyKey) {
    return repository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey).map(this::toDomain);
  }

  @Override
  public Optional<PaymentIntent> findByExternalId(String externalId) {
    return repository.findByExternalId(externalId).map(this::toDomain);
  }

  @Override
  public List<PaymentIntent> findByTenantIdAndCreatedAtBetween(
      String tenantId, Instant start, Instant end) {
    return repository.findByTenantIdAndCreatedAtBetween(tenantId, start, end).stream()
        .map((PaymentIntentEntity e) -> this.toDomain(e))
        .toList();
  }

  @Override
  public List<PaymentIntent> findNextPage(
      String tenantId, Instant lastCreatedAt, UUID lastId, int limit) {
    List<PaymentIntentEntity> entities;
    if (lastCreatedAt == null || lastId == null) {
      entities =
          repository.findFirstPage(
              tenantId, org.springframework.data.domain.PageRequest.of(0, limit));
    } else {
      entities =
          repository.findNextPage(
              tenantId,
              lastCreatedAt,
              lastId,
              org.springframework.data.domain.PageRequest.of(0, limit));
    }
    return entities.stream().map((PaymentIntentEntity e) -> this.toDomain(e)).toList();
  }

  private PaymentIntentEntity toEntity(PaymentIntent intent) {
    PaymentIntentEntity entity = new PaymentIntentEntity();
    entity.setId(intent.getIntentId());
    entity.setTenantId(intent.getTenantId());
    entity.setOrderId(intent.getOrderId());
    entity.setAmount(intent.getAmount());
    entity.setProvider(intent.getProvider());
    entity.setStatus(intent.getStatus());
    entity.setIdempotencyKey(intent.getIdempotencyKey());
    entity.setExternalId(intent.getExternalId());
    entity.setCheckoutUrl(intent.getCheckoutUrl());
    entity.setClientSecret(intent.getClientSecret());
    entity.setFailureCode(intent.getFailureCode());
    entity.setFailureMessage(intent.getFailureMessage());
    entity.setQrCode(intent.getQrCode());
    entity.setDeepLink(intent.getDeepLink());
    entity.setCreatedAt(intent.getCreatedAt());
    entity.setUpdatedAt(intent.getUpdatedAt());
    entity.setVersion(intent.getVersion());
    return entity;
  }

  private PaymentIntent toDomain(PaymentIntentEntity entity) {
    PaymentIntent intent =
        new PaymentIntent(
            entity.getId(),
            entity.getTenantId(),
            entity.getOrderId(),
            entity.getAmount(),
            entity.getProvider(),
            entity.getIdempotencyKey());

    if (entity.getExternalId() != null) {
      intent.start(
          entity.getExternalId(),
          entity.getCheckoutUrl(),
          entity.getClientSecret(),
          entity.getQrCode(),
          entity.getDeepLink());
    }

    if (entity.getStatus() == PaymentStatus.SUCCEEDED) {
      intent.succeed(entity.getExternalId());
    } else if (entity.getStatus() == PaymentStatus.FAILED) {
      intent.fail(entity.getFailureCode(), entity.getFailureMessage());
    }

    intent.setVersion(entity.getVersion());
    return intent;
  }

  @Repository
  interface PaymentIntentSpringRepository extends JpaRepository<PaymentIntentEntity, UUID> {
    Optional<PaymentIntentEntity> findByTenantIdAndIdempotencyKey(
        String tenantId, String idempotencyKey);

    Optional<PaymentIntentEntity> findByExternalId(String externalId);

    List<PaymentIntentEntity> findByTenantIdAndCreatedAtBetween(
        String tenantId, Instant start, Instant end);

    @org.springframework.data.jpa.repository.Query(
        "SELECT e FROM PaymentIntentEntity e WHERE e.tenantId = :tenantId ORDER BY e.createdAt"
            + " DESC, e.id DESC")
    List<PaymentIntentEntity> findFirstPage(
        @org.springframework.data.repository.query.Param("tenantId") String tenantId,
        org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
        "SELECT e FROM PaymentIntentEntity e WHERE e.tenantId = :tenantId AND (e.createdAt <"
            + " :lastCreatedAt OR (e.createdAt = :lastCreatedAt AND e.id < :lastId)) ORDER BY"
            + " e.createdAt DESC, e.id DESC")
    List<PaymentIntentEntity> findNextPage(
        @org.springframework.data.repository.query.Param("tenantId") String tenantId,
        @org.springframework.data.repository.query.Param("lastCreatedAt") Instant lastCreatedAt,
        @org.springframework.data.repository.query.Param("lastId") UUID lastId,
        org.springframework.data.domain.Pageable pageable);
  }
}
