package com.chamrong.iecommerce.helpdesk.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for POST /api/v1/helpdesk/tickets/:id/reply. */
public record ReplyRequest(@NotBlank(message = "body is required") String body) {}
