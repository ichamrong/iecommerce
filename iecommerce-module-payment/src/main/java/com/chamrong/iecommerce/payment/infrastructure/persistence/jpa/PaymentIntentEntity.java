package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.PaymentStatus;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "payment_intent",
    indexes = {
      @Index(name = "idx_payment_intent_tenant_created", columnList = "tenant_id, created_at, id"),
      @Index(name = "idx_payment_intent_external_id", columnList = "external_id"),
      @Index(
          name = "idx_payment_intent_idempotency",
          columnList = "tenant_id, idempotency_key",
          unique = true)
    })
public class PaymentIntentEntity {

  protected PaymentIntentEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Money getAmount() {
    return amount;
  }

  public void setAmount(Money amount) {
    this.amount = amount;
  }

  public ProviderType getProvider() {
    return provider;
  }

  public void setProvider(ProviderType provider) {
    this.provider = provider;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getCheckoutUrl() {
    return checkoutUrl;
  }

  public void setCheckoutUrl(String checkoutUrl) {
    this.checkoutUrl = checkoutUrl;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getFailureCode() {
    return failureCode;
  }

  public void setFailureCode(String failureCode) {
    this.failureCode = failureCode;
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  public void setFailureMessage(String failureMessage) {
    this.failureMessage = failureMessage;
  }

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public String getDeepLink() {
    return deepLink;
  }

  public void setDeepLink(String deepLink) {
    this.deepLink = deepLink;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public PaymentIntentEntity(
      UUID id,
      String tenantId,
      Long orderId,
      Money amount,
      ProviderType provider,
      PaymentStatus status,
      String idempotencyKey) {
    this.id = Objects.requireNonNull(id);
    this.tenantId = Objects.requireNonNull(tenantId);
    this.orderId = Objects.requireNonNull(orderId);
    this.amount = Objects.requireNonNull(amount);
    this.provider = Objects.requireNonNull(provider);
    this.status = Objects.requireNonNull(status);
    this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @Id
  @Column(columnDefinition = "UUID")
  private UUID id;

  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ProviderType provider;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private PaymentStatus status;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @Column(name = "external_id", length = 100)
  private String externalId;

  @Column(name = "checkout_url", columnDefinition = "TEXT")
  private String checkoutUrl;

  @Column(name = "client_secret", columnDefinition = "TEXT")
  private String clientSecret;

  @Column(name = "failure_code", length = 50)
  private String failureCode;

  @Column(name = "failure_message", columnDefinition = "TEXT")
  private String failureMessage;

  @Column(name = "qr_code", columnDefinition = "TEXT")
  private String qrCode;

  @Column(name = "deep_link", columnDefinition = "TEXT")
  private String deepLink;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  private Long version;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
      return false;
    PaymentIntentEntity that = (PaymentIntentEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
