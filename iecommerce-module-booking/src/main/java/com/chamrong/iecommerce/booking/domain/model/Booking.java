package com.chamrong.iecommerce.booking.domain.model;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Booking aggregate: pure domain, no JPA. Supports ACCOMMODATION (night-based) and APPOINTMENT
 * (time-slot).
 *
 * <p>State machine: PENDING_CONFIRMATION → CONFIRMED → CHECKED_IN → CHECKED_OUT, or CANCELLED.
 */
public final class Booking {

  private final Long id;
  private final String tenantId;
  private final Instant createdAt;
  private final Long version;
  private final BookingKind kind;
  private final Long resourceProductId;
  private final Long resourceVariantId;
  private final Long resourceId; // for appointment: staff/room
  private final Long customerId;
  private final Long assignedStaffId;
  private final Instant startAt;
  private final Instant endAt;
  private final BookingStatus status;
  private final BookingSource source;
  private final GuestInfo guestInfo;
  private final Money totalPrice;
  private final PricingBreakdown pricingBreakdown;
  private final String customerNotes;
  private final String internalNotes;
  private final String holdToken;
  private final String idempotencyKey;
  private final List<BookingItem> items;

  private Booking(Builder b) {
    this.id = b.id;
    this.tenantId = Objects.requireNonNull(b.tenantId, "tenantId");
    this.createdAt = b.createdAt != null ? b.createdAt : Instant.now();
    this.version = b.version;
    this.kind = Objects.requireNonNull(b.kind, "kind");
    this.resourceProductId = Objects.requireNonNull(b.resourceProductId, "resourceProductId");
    this.resourceVariantId = b.resourceVariantId;
    this.resourceId = b.resourceId;
    this.customerId = Objects.requireNonNull(b.customerId, "customerId");
    this.assignedStaffId = b.assignedStaffId;
    this.startAt = Objects.requireNonNull(b.startAt, "startAt");
    this.endAt = Objects.requireNonNull(b.endAt, "endAt");
    this.status = b.status != null ? b.status : BookingStatus.PENDING;
    this.source = b.source != null ? b.source : BookingSource.WEB;
    this.guestInfo = b.guestInfo != null ? b.guestInfo : GuestInfo.empty();
    this.totalPrice = b.totalPrice;
    this.pricingBreakdown = b.pricingBreakdown;
    this.customerNotes = b.customerNotes;
    this.internalNotes = b.internalNotes;
    this.holdToken = b.holdToken;
    this.idempotencyKey = b.idempotencyKey;
    this.items = b.items != null ? List.copyOf(b.items) : List.of();
  }

  public Long getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Long getVersion() {
    return version;
  }

  public BookingKind getKind() {
    return kind;
  }

  public Long getResourceProductId() {
    return resourceProductId;
  }

  public Long getResourceVariantId() {
    return resourceVariantId;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public Long getAssignedStaffId() {
    return assignedStaffId;
  }

  public Instant getStartAt() {
    return startAt;
  }

  public Instant getEndAt() {
    return endAt;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public BookingSource getSource() {
    return source;
  }

  public GuestInfo getGuestInfo() {
    return guestInfo;
  }

  public Money getTotalPrice() {
    return totalPrice;
  }

  public PricingBreakdown getPricingBreakdown() {
    return pricingBreakdown;
  }

  public String getCustomerNotes() {
    return customerNotes;
  }

  public String getInternalNotes() {
    return internalNotes;
  }

  public String getHoldToken() {
    return holdToken;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public List<BookingItem> getItems() {
    return items;
  }

  public boolean canConfirm() {
    return status == BookingStatus.PENDING;
  }

  public boolean canModify() {
    return status == BookingStatus.PENDING || status == BookingStatus.ACCEPTED;
  }

  public boolean canCancel() {
    return status != BookingStatus.CHECKOUT
        && status != BookingStatus.CANCEL
        && status != BookingStatus.REJECTED;
  }

  public boolean canCheckIn() {
    return status == BookingStatus.ACCEPTED;
  }

  public boolean canCheckOut() {
    return status == BookingStatus.CHECKIN;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Long id;
    private String tenantId;
    private Instant createdAt;
    private Long version;
    private BookingKind kind;
    private Long resourceProductId;
    private Long resourceVariantId;
    private Long resourceId;
    private Long customerId;
    private Long assignedStaffId;
    private Instant startAt;
    private Instant endAt;
    private BookingStatus status;
    private BookingSource source;
    private GuestInfo guestInfo;
    private Money totalPrice;
    private PricingBreakdown pricingBreakdown;
    private String customerNotes;
    private String internalNotes;
    private String holdToken;
    private String idempotencyKey;
    private List<BookingItem> items;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder version(Long version) {
      this.version = version;
      return this;
    }

    public Builder kind(BookingKind kind) {
      this.kind = kind;
      return this;
    }

    public Builder resourceProductId(Long resourceProductId) {
      this.resourceProductId = resourceProductId;
      return this;
    }

    public Builder resourceVariantId(Long resourceVariantId) {
      this.resourceVariantId = resourceVariantId;
      return this;
    }

    public Builder resourceId(Long resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder customerId(Long customerId) {
      this.customerId = customerId;
      return this;
    }

    public Builder assignedStaffId(Long assignedStaffId) {
      this.assignedStaffId = assignedStaffId;
      return this;
    }

    public Builder startAt(Instant startAt) {
      this.startAt = startAt;
      return this;
    }

    public Builder endAt(Instant endAt) {
      this.endAt = endAt;
      return this;
    }

    public Builder status(BookingStatus status) {
      this.status = status;
      return this;
    }

    public Builder source(BookingSource source) {
      this.source = source;
      return this;
    }

    public Builder guestInfo(GuestInfo guestInfo) {
      this.guestInfo = guestInfo;
      return this;
    }

    public Builder totalPrice(Money totalPrice) {
      this.totalPrice = totalPrice;
      return this;
    }

    public Builder pricingBreakdown(PricingBreakdown pricingBreakdown) {
      this.pricingBreakdown = pricingBreakdown;
      return this;
    }

    public Builder customerNotes(String customerNotes) {
      this.customerNotes = customerNotes;
      return this;
    }

    public Builder internalNotes(String internalNotes) {
      this.internalNotes = internalNotes;
      return this;
    }

    public Builder holdToken(String holdToken) {
      this.holdToken = holdToken;
      return this;
    }

    public Builder idempotencyKey(String idempotencyKey) {
      this.idempotencyKey = idempotencyKey;
      return this;
    }

    public Builder items(List<BookingItem> items) {
      this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
      return this;
    }

    public Booking build() {
      return new Booking(this);
    }
  }
}
