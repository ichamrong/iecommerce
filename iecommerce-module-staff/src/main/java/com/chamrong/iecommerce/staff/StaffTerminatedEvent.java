package com.chamrong.iecommerce.staff;

/** Event published when a staff member is terminated. */
public record StaffTerminatedEvent(String tenantId, Long staffId) {}
