package com.chamrong.iecommerce.customer.application.dto;

import java.util.List;

public record CustomerResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    Long authUserId,
    List<AddressResponse> addresses) {}
