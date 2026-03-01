package com.chamrong.iecommerce.booking.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.booking.domain.Booking;
import com.chamrong.iecommerce.booking.domain.model.BookingKind;
import com.chamrong.iecommerce.booking.domain.model.BookingStatus;
import com.chamrong.iecommerce.booking.domain.model.GuestInfo;
import com.chamrong.iecommerce.booking.domain.model.PricingBreakdown;
import org.springframework.stereotype.Component;

/**
 * Maps between domain.Booking (JPA entity) and domain.model.Booking (pure).
 */
@Component
public class BookingEntityMapper {

  public com.chamrong.iecommerce.booking.domain.model.Booking toDomain(Booking entity) {
    if (entity == null) return null;
    return com.chamrong.iecommerce.booking.domain.model.Booking.builder()
        .id(entity.getId())
        .tenantId(entity.getTenantId())
        .createdAt(entity.getCreatedAt())
        .version(null)
        .kind(entity.getType() != null ? BookingKind.valueOf(entity.getType().name()) : BookingKind.ACCOMMODATION)
        .resourceProductId(entity.getResourceProductId())
        .resourceVariantId(entity.getResourceVariantId())
        .resourceId(null)
        .customerId(entity.getCustomerId())
        .assignedStaffId(entity.getAssignedStaffId())
        .startAt(entity.getStartAt())
        .endAt(entity.getEndAt())
        .status(mapStatus(entity.getStatus()))
        .source(com.chamrong.iecommerce.booking.domain.model.BookingSource.WEB)
        .guestInfo(new GuestInfo(
            entity.getGuestFirstName(),
            entity.getGuestLastName(),
            entity.getGuestEmail(),
            entity.getGuestPhone()))
        .totalPrice(entity.getTotalPrice())
        .pricingBreakdown(entity.getTotalPrice() != null ? PricingBreakdown.of(entity.getTotalPrice()) : null)
        .customerNotes(entity.getCustomerNotes())
        .internalNotes(entity.getInternalNotes())
        .holdToken(null)
        .idempotencyKey(null)
        .items(java.util.List.of())
        .build();
  }

  private static BookingStatus mapStatus(com.chamrong.iecommerce.booking.domain.BookingStatus s) {
    if (s == null) return BookingStatus.PENDING;
    return switch (s) {
      case PENDING -> BookingStatus.PENDING;
      case ACCEPTED -> BookingStatus.ACCEPTED;
      case CHECKIN -> BookingStatus.CHECKIN;
      case CHECKOUT -> BookingStatus.CHECKOUT;
      case CANCEL -> BookingStatus.CANCEL;
      case REJECTED -> BookingStatus.REJECTED;
    };
  }
}
