package com.chamrong.iecommerce.booking;

public record BookingAutoCancelledEvent(
    String tenantId, Long bookingId, Long customerId, String guestEmail) {}
