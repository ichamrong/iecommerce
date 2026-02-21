package com.chamrong.iecommerce.staff;

/** Event published when a staff member is suspended. */
public record StaffSuspendedEvent(String tenantId, Long staffId) {}
