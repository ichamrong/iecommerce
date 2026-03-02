package com.chamrong.iecommerce.chat.api;

import com.chamrong.iecommerce.chat.application.ChatService;
import com.chamrong.iecommerce.chat.application.dto.ChatMessageResponse;
import com.chamrong.iecommerce.chat.application.dto.ConversationResponse;
import com.chamrong.iecommerce.chat.application.dto.SendMessageRequest;
import com.chamrong.iecommerce.chat.application.dto.StartConversationRequest;
import com.chamrong.iecommerce.chat.application.query.ConversationQueryService;
import com.chamrong.iecommerce.chat.application.query.MessageQueryService;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.security.TenantGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Real-time messaging — conversations and messages.
 *
 * <p>Base path: {@code /api/v1/chat}
 */
@Tag(name = "Chat", description = "Conversation and messaging between users")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

  private final ChatService chatService;
  private final ConversationQueryService conversationQueryService;
  private final MessageQueryService messageQueryService;

  @Operation(summary = "Start a new conversation")
  @PostMapping("/conversations")
  public ResponseEntity<ConversationResponse> start(
      @RequestParam String tenantId, @RequestBody StartConversationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(chatService.startConversation(tenantId, req));
  }

  @Operation(
      summary = "List my conversations",
      description = "Cursor-paginated. Returns conversations where the user is a participant.")
  @GetMapping("/conversations")
  public CursorPageResponse<ConversationResponse> myConversations(
      @RequestParam Long userId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    return conversationQueryService.findPage(tenantId, userId, cursor, limit);
  }

  @Operation(summary = "Get a single conversation")
  @GetMapping("/conversations/{id}")
  public ResponseEntity<ConversationResponse> getConversation(
      @PathVariable Long id, @RequestParam Long actorId) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    return conversationQueryService
        .findById(tenantId, id, actorId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get messages in a conversation (cursor-paginated)")
  @GetMapping("/conversations/{conversationId}/messages")
  public CursorPageResponse<ChatMessageResponse> getMessages(
      @PathVariable Long conversationId,
      @RequestParam Long actorId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "50") int limit) {
    String tenantId = TenantGuard.requireTenantIdPresent();
    return messageQueryService.findPage(tenantId, conversationId, actorId, cursor, limit);
  }

  @Operation(
      summary = "Send a message",
      description = "Appends a message. The sender must be a participant.")
  @PostMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<ChatMessageResponse> send(
      @PathVariable Long conversationId,
      @RequestParam String tenantId,
      @RequestBody SendMessageRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(chatService.sendMessage(tenantId, conversationId, req));
  }
}
