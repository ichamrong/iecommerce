package com.chamrong.iecommerce.helpdesk.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.helpdesk.application.HelpdeskService;
import com.chamrong.iecommerce.helpdesk.application.dto.ReplyRequest;
import com.chamrong.iecommerce.helpdesk.application.dto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for helpdesk tickets. */
@Tag(name = "Helpdesk", description = "Support ticket management")
@RestController
@RequestMapping("/api/v1/helpdesk/tickets")
@RequiredArgsConstructor
public class HelpdeskController {

  private final HelpdeskService service;

  @Operation(summary = "List tickets")
  @GetMapping
  @PreAuthorize(Permissions.HAS_HELPDESK_READ)
  public List<TicketResponse> list() {
    return service.listTickets();
  }

  @Operation(summary = "Get ticket by id")
  @GetMapping("/{id}")
  @PreAuthorize(Permissions.HAS_HELPDESK_READ)
  public ResponseEntity<TicketResponse> getById(@PathVariable String id) {
    return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Reply to ticket")
  @PostMapping("/{id}/reply")
  @PreAuthorize(Permissions.HAS_HELPDESK_REPLY)
  public ResponseEntity<TicketResponse> reply(
      @PathVariable String id, @Valid @RequestBody ReplyRequest request) {
    try {
      return ResponseEntity.ok(service.reply(id, request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(summary = "Close ticket")
  @PostMapping("/{id}/close")
  @PreAuthorize(Permissions.HAS_HELPDESK_REPLY)
  public ResponseEntity<TicketResponse> close(@PathVariable String id) {
    try {
      return ResponseEntity.ok(service.close(id));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
