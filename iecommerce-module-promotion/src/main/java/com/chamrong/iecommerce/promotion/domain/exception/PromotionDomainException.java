package com.chamrong.iecommerce.promotion.domain.exception;

/** Exception thrown for domain-specific promotion errors. */
public class PromotionDomainException extends RuntimeException {
  public PromotionDomainException(String message) {
    super(message);
  }
}
