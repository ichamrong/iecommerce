package com.chamrong.iecommerce.booking.domain.exception;

/** Base domain exception for booking operations. */
public class BookingDomainException extends RuntimeException {

  private final String errorCode;

  public BookingDomainException(String message) {
    super(message);
    this.errorCode = "BOOKING_ERROR";
  }

  public BookingDomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode != null ? errorCode : "BOOKING_ERROR";
  }

  public String getErrorCode() {
    return errorCode;
  }
}
