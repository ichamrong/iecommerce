package com.chamrong.iecommerce.chat.api;

import com.chamrong.iecommerce.chat.application.ChatService;
import com.chamrong.iecommerce.chat.application.dto.ChatMessageResponse;
import com.chamrong.iecommerce.chat.application.dto.ConversationResponse;
import com.chamrong.iecommerce.chat.application.dto.SendMessageRequest;
import com.chamrong.iecommerce.chat.application.dto.StartConversationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

  @Operation(summary = "Start a new conversation")
  @PostMapping("/conversations")
  public ResponseEntity<ConversationResponse> start(
      @RequestParam String tenantId, @RequestBody StartConversationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(chatService.startConversation(tenantId, req));
  }

  @Operation(
      summary = "List my conversations",
      description = "Returns all conversations where the given user is a participant.")
  @GetMapping("/conversations")
  public List<ConversationResponse> myConversations(@RequestParam Long userId) {
    return chatService.getMyConversations(userId);
  }

  @Operation(summary = "Get messages in a conversation")
  @GetMapping("/conversations/{conversationId}/messages")
  public List<ChatMessageResponse> getMessages(@PathVariable Long conversationId) {
    return chatService.getMessages(conversationId);
  }

  @Operation(
      summary = "Send a message",
      description = "Appends a message to the conversation. The sender must be a participant.")
  @PostMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<ChatMessageResponse> send(
      @PathVariable Long conversationId, @RequestBody SendMessageRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(chatService.sendMessage(conversationId, req));
  }
}
