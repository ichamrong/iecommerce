package com.chamrong.iecommerce.booking.domain.service;

import com.chamrong.iecommerce.booking.domain.model.Booking;
import com.chamrong.iecommerce.booking.domain.model.BookingStatus;

/**
 * Validates and executes booking state transitions. Pure domain logic.
 */
public final class BookingStateMachine {

  private BookingStateMachine() {}

  public static Booking confirm(Booking booking) {
    if (!booking.canConfirm()) {
      throw new IllegalStateException(
          "Cannot confirm booking in status: " + booking.getStatus());
    }
    return Booking.builder()
        .id(booking.getId())
        .tenantId(booking.getTenantId())
        .createdAt(booking.getCreatedAt())
        .version(booking.getVersion())
        .kind(booking.getKind())
        .resourceProductId(booking.getResourceProductId())
        .resourceVariantId(booking.getResourceVariantId())
        .resourceId(booking.getResourceId())
        .customerId(booking.getCustomerId())
        .assignedStaffId(booking.getAssignedStaffId())
        .startAt(booking.getStartAt())
        .endAt(booking.getEndAt())
        .status(BookingStatus.ACCEPTED)
        .source(booking.getSource())
        .guestInfo(booking.getGuestInfo())
        .totalPrice(booking.getTotalPrice())
        .pricingBreakdown(booking.getPricingBreakdown())
        .customerNotes(booking.getCustomerNotes())
        .internalNotes(booking.getInternalNotes())
        .holdToken(booking.getHoldToken())
        .idempotencyKey(booking.getIdempotencyKey())
        .items(booking.getItems())
        .build();
  }

  public static Booking checkIn(Booking booking) {
    if (!booking.canCheckIn()) {
      throw new IllegalStateException(
          "Cannot check-in booking in status: " + booking.getStatus());
    }
    return Booking.builder()
        .id(booking.getId())
        .tenantId(booking.getTenantId())
        .createdAt(booking.getCreatedAt())
        .version(booking.getVersion())
        .kind(booking.getKind())
        .resourceProductId(booking.getResourceProductId())
        .resourceVariantId(booking.getResourceVariantId())
        .resourceId(booking.getResourceId())
        .customerId(booking.getCustomerId())
        .assignedStaffId(booking.getAssignedStaffId())
        .startAt(booking.getStartAt())
        .endAt(booking.getEndAt())
        .status(BookingStatus.CHECKIN)
        .source(booking.getSource())
        .guestInfo(booking.getGuestInfo())
        .totalPrice(booking.getTotalPrice())
        .pricingBreakdown(booking.getPricingBreakdown())
        .customerNotes(booking.getCustomerNotes())
        .internalNotes(booking.getInternalNotes())
        .holdToken(booking.getHoldToken())
        .idempotencyKey(booking.getIdempotencyKey())
        .items(booking.getItems())
        .build();
  }

  public static Booking checkOut(Booking booking) {
    if (!booking.canCheckOut()) {
      throw new IllegalStateException(
          "Cannot check-out booking in status: " + booking.getStatus());
    }
    return Booking.builder()
        .id(booking.getId())
        .tenantId(booking.getTenantId())
        .createdAt(booking.getCreatedAt())
        .version(booking.getVersion())
        .kind(booking.getKind())
        .resourceProductId(booking.getResourceProductId())
        .resourceVariantId(booking.getResourceVariantId())
        .resourceId(booking.getResourceId())
        .customerId(booking.getCustomerId())
        .assignedStaffId(booking.getAssignedStaffId())
        .startAt(booking.getStartAt())
        .endAt(booking.getEndAt())
        .status(BookingStatus.CHECKOUT)
        .source(booking.getSource())
        .guestInfo(booking.getGuestInfo())
        .totalPrice(booking.getTotalPrice())
        .pricingBreakdown(booking.getPricingBreakdown())
        .customerNotes(booking.getCustomerNotes())
        .internalNotes(booking.getInternalNotes())
        .holdToken(booking.getHoldToken())
        .idempotencyKey(booking.getIdempotencyKey())
        .items(booking.getItems())
        .build();
  }

  public static Booking cancel(Booking booking, String reason) {
    if (!booking.canCancel()) {
      throw new IllegalStateException(
          "Cannot cancel booking in status: " + booking.getStatus());
    }
    return Booking.builder()
        .id(booking.getId())
        .tenantId(booking.getTenantId())
        .createdAt(booking.getCreatedAt())
        .version(booking.getVersion())
        .kind(booking.getKind())
        .resourceProductId(booking.getResourceProductId())
        .resourceVariantId(booking.getResourceVariantId())
        .resourceId(booking.getResourceId())
        .customerId(booking.getCustomerId())
        .assignedStaffId(booking.getAssignedStaffId())
        .startAt(booking.getStartAt())
        .endAt(booking.getEndAt())
        .status(BookingStatus.CANCEL)
        .source(booking.getSource())
        .guestInfo(booking.getGuestInfo())
        .totalPrice(booking.getTotalPrice())
        .pricingBreakdown(booking.getPricingBreakdown())
        .customerNotes(booking.getCustomerNotes())
        .internalNotes(reason != null && !reason.isBlank() ? reason : booking.getInternalNotes())
        .holdToken(booking.getHoldToken())
        .idempotencyKey(booking.getIdempotencyKey())
        .items(booking.getItems())
        .build();
  }
}
