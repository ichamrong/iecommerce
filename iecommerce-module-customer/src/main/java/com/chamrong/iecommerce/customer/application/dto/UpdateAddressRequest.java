package com.chamrong.iecommerce.customer.application.dto;

/** DTO for updating an existing customer address. */
public record UpdateAddressRequest(
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    Boolean isDefaultShipping,
    Boolean isDefaultBilling) {}
