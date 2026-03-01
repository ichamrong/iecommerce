package com.chamrong.iecommerce.booking.domain.event;

/**
 * Published when a guest checks in.
 *
 * @param tenantId   tenant
 * @param bookingId  booking id
 */
public record GuestCheckedInEvent(String tenantId, Long bookingId) {}
