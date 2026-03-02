package com.chamrong.iecommerce.chat.domain.model;

/** Type of conversation for routing and access control. */
public enum ConversationType {

  /** Customer support: customer ↔ staff. */
  SUPPORT,

  /** Internal: staff ↔ staff. */
  INTERNAL,

  /** Linked to an order. */
  ORDER,

  /** Linked to a booking. */
  BOOKING,

  /** POS/terminal support messages. */
  POS
}
