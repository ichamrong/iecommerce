package com.chamrong.iecommerce.staff;

/** Event published when a new staff member is created. */
public record StaffCreatedEvent(String tenantId, Long staffId, String email) {}
