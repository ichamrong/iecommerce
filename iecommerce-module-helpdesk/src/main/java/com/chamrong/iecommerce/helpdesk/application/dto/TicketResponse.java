package com.chamrong.iecommerce.helpdesk.application.dto;

import com.chamrong.iecommerce.helpdesk.domain.TicketStatus;
import java.time.Instant;
import java.util.List;

/** API response for a ticket. */
public record TicketResponse(
    String id,
    String tenantName,
    String subject,
    TicketStatus status,
    Instant createdAt,
    List<TicketMessageResponse> messages) {}
