package com.chamrong.iecommerce.helpdesk.application;

import com.chamrong.iecommerce.helpdesk.application.dto.ReplyRequest;
import com.chamrong.iecommerce.helpdesk.application.dto.TicketMessageResponse;
import com.chamrong.iecommerce.helpdesk.application.dto.TicketResponse;
import com.chamrong.iecommerce.helpdesk.domain.Ticket;
import com.chamrong.iecommerce.helpdesk.domain.TicketMessage;
import com.chamrong.iecommerce.helpdesk.domain.TicketStatus;
import com.chamrong.iecommerce.helpdesk.domain.ports.TicketRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for helpdesk tickets. */
@Service
@RequiredArgsConstructor
@Slf4j
public class HelpdeskService {

  private final TicketRepositoryPort repository;

  @Transactional(readOnly = true)
  public List<TicketResponse> listTickets() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public Optional<TicketResponse> getById(String id) {
    return repository.findById(id).map(this::toResponse);
  }

  @Transactional
  public TicketResponse reply(String id, ReplyRequest request) {
    Ticket ticket =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    TicketMessage msg =
        TicketMessage.builder()
            .id(UUID.randomUUID().toString())
            .from("staff")
            .body(request.body())
            .at(Instant.now())
            .build();
    ticket.getMessages().add(msg);
    repository.save(ticket);
    log.debug("Reply added to ticket {}", id);
    return toResponse(ticket);
  }

  @Transactional
  public TicketResponse close(String id) {
    Ticket ticket =
        repository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    ticket.setStatus(TicketStatus.CLOSED);
    repository.save(ticket);
    log.info("Ticket {} closed", id);
    return toResponse(ticket);
  }

  private TicketResponse toResponse(Ticket t) {
    List<TicketMessageResponse> msgResponses =
        t.getMessages() == null
            ? List.of()
            : t.getMessages().stream()
                .map(m -> new TicketMessageResponse(m.getId(), m.getFrom(), m.getBody(), m.getAt()))
                .toList();
    return new TicketResponse(
        t.getId(),
        t.getTenantName(),
        t.getSubject(),
        t.getStatus(),
        t.getCreatedAt(),
        msgResponses);
  }
}
