package com.chamrong.iecommerce.review.application.dto;

public record ReviewRequest(
    Long productId,
    Long customerId,
    Long bookingId,
    boolean isAnonymous,
    Integer rating,
    Integer cleanlinessRating,
    Integer accuracyRating,
    Integer communicationRating,
    Integer locationRating,
    Integer checkInRating,
    Integer valueRating,
    String comment,
    String mediaKeys) {}
