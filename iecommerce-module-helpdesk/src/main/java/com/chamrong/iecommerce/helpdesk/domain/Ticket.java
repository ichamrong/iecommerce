package com.chamrong.iecommerce.helpdesk.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Domain model for a helpdesk ticket. */
@Getter
@Setter
@Builder
public class Ticket {

  private String id;
  private String tenantName;
  private String subject;
  private TicketStatus status;
  private Instant createdAt;
  @Builder.Default private List<TicketMessage> messages = new ArrayList<>();
}
