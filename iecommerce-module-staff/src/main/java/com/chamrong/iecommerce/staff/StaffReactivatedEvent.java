package com.chamrong.iecommerce.staff;

/** Event published when a staff member is reactivated. */
public record StaffReactivatedEvent(String tenantId, Long staffId) {}
