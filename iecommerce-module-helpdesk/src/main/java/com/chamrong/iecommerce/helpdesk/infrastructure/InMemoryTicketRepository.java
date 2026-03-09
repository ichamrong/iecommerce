package com.chamrong.iecommerce.helpdesk.infrastructure;

import com.chamrong.iecommerce.helpdesk.domain.Ticket;
import com.chamrong.iecommerce.helpdesk.domain.TicketMessage;
import com.chamrong.iecommerce.helpdesk.domain.TicketStatus;
import com.chamrong.iecommerce.helpdesk.domain.ports.TicketRepositoryPort;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory implementation of ticket repository. For production, replace with JPA. */
@Component
public class InMemoryTicketRepository implements TicketRepositoryPort {

  private final Map<String, Ticket> store = new ConcurrentHashMap<>();

  @PostConstruct
  void seedSample() {
    if (store.isEmpty()) {
      Ticket t =
          Ticket.builder()
              .id("ticket-1")
              .tenantName("Acme Store")
              .subject("Cannot access dashboard")
              .status(TicketStatus.OPEN)
              .createdAt(Instant.now())
              .messages(
                  List.of(
                      TicketMessage.builder()
                          .id("msg-1")
                          .from("tenant")
                          .body("After login I get a blank page.")
                          .at(Instant.now())
                          .build()))
              .build();
      store.put(t.getId(), t);
    }
  }

  @Override
  public List<Ticket> findAll() {
    return store.values().stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .toList();
  }

  @Override
  public Optional<Ticket> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Ticket save(Ticket ticket) {
    store.put(ticket.getId(), ticket);
    return ticket;
  }
}
