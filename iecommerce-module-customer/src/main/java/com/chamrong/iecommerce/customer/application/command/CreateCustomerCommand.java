package com.chamrong.iecommerce.customer.application.command;

public record CreateCustomerCommand(
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    Long authUserId,
    String tenantId) {} // Optional for now, since auth events might pass it
