package com.chamrong.iecommerce.customer;

/** Event published when a new customer is created. */
public record CustomerCreatedEvent(String tenantId, Long customerId, String email) {}
