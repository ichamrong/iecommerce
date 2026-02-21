package com.chamrong.iecommerce.customer;

/** Event published when a customer is updated. */
public record CustomerUpdatedEvent(String tenantId, Long customerId) {}
