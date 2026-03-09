package com.chamrong.iecommerce.helpdesk.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** A single message in a ticket thread. */
@Getter
@Setter
@Builder
public class TicketMessage {

  private String id;
  private String from; // "tenant" | "staff"
  private String body;
  private Instant at;
}
