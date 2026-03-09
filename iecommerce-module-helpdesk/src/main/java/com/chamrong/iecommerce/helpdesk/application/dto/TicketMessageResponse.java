package com.chamrong.iecommerce.helpdesk.application.dto;

import java.time.Instant;

/** API response for a ticket message. */
public record TicketMessageResponse(String id, String from, String body, Instant at) {}
