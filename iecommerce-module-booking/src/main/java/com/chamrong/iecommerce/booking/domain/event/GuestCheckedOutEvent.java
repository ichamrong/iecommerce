package com.chamrong.iecommerce.booking.domain.event;

/**
 * Published when a guest checks out. Triggers invoice issuance and payment settlement.
 *
 * @param tenantId   tenant
 * @param bookingId  booking id
 */
public record GuestCheckedOutEvent(String tenantId, Long bookingId) {}
