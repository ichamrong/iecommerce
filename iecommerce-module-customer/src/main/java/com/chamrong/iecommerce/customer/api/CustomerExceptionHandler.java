package com.chamrong.iecommerce.customer.api;

import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Handles exceptions for customer API (cursor, validation, etc.). */
@RestControllerAdvice(basePackages = "com.chamrong.iecommerce.customer")
public class CustomerExceptionHandler {

  @ExceptionHandler(InvalidCursorException.class)
  public ResponseEntity<CustomerErrorResponse> handleInvalidCursor(InvalidCursorException ex) {
    String code = ex.getErrorCode() != null ? ex.getErrorCode() : "INVALID_CURSOR";
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new CustomerErrorResponse(ex.getMessage(), code));
  }
}
