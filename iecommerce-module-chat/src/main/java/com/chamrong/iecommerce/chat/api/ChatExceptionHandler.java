package com.chamrong.iecommerce.chat.api;

import com.chamrong.iecommerce.chat.domain.exception.ChatDomainException;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.chamrong.iecommerce.chat.api")
@Slf4j
public class ChatExceptionHandler {

  @ExceptionHandler(InvalidCursorException.class)
  public ResponseEntity<ChatErrorResponse> handleInvalidCursor(InvalidCursorException ex) {
    log.debug("[Chat] Invalid cursor: {} - {}", ex.getErrorCode(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ChatErrorResponse(ex.getMessage(), ex.getErrorCode() != null ? ex.getErrorCode() : "INVALID_CURSOR"));
  }

  @ExceptionHandler(ChatDomainException.class)
  public ResponseEntity<ChatErrorResponse> handleChatDomain(ChatDomainException ex) {
    log.warn("[Chat] Domain: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ChatErrorResponse(ex.getMessage(), "CHAT_ACCESS_DENIED"));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ChatErrorResponse> handleNotFound(EntityNotFoundException ex) {
    log.debug("[Chat] Not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ChatErrorResponse(ex.getMessage(), "CHAT_NOT_FOUND"));
  }

  public record ChatErrorResponse(String message, String code) {}
}
