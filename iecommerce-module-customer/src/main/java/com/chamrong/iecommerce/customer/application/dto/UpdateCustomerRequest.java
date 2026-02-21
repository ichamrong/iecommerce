package com.chamrong.iecommerce.customer.application.dto;

public record UpdateCustomerRequest(
    String firstName,
    String lastName,
    String phoneNumber,
    java.time.LocalDate dateOfBirth,
    String gender) {}
