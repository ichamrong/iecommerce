package com.chamrong.iecommerce.helpdesk.domain.ports;

import com.chamrong.iecommerce.helpdesk.domain.Ticket;
import java.util.List;
import java.util.Optional;

/** Port for ticket persistence. */
public interface TicketRepositoryPort {

  List<Ticket> findAll();

  Optional<Ticket> findById(String id);

  Ticket save(Ticket ticket);
}
