package com.chamrong.iecommerce.review.application.dto;

import java.time.Instant;

public record ReviewResponse(
    Long id,
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
    String mediaKeys,
    String status,
    boolean flaggedByOwner,
    String flagReason,
    String ownerReply,
    Instant createdAt) {}
