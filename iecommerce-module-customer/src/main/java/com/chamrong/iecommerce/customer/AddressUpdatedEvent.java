package com.chamrong.iecommerce.customer;

/** Event published when a customer address is updated. */
public record AddressUpdatedEvent(String tenantId, Long customerId, Long addressId) {}
