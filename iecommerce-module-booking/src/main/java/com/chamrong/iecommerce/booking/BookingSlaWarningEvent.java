package com.chamrong.iecommerce.booking;

public record BookingSlaWarningEvent(String tenantId, Long bookingId, Long hostId) {}
