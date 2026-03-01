package com.chamrong.iecommerce.common.pagination;

/**
 * Thrown when a cursor string is invalid, unsupported version, or filter hash mismatch.
 *
 * <p>Use {@link #getErrorCode()} for stable API error codes (e.g. RFC 7807 {@code errorCode}).
 */
public final class InvalidCursorException extends RuntimeException {

  /** Cursor is malformed, not Base64, or not valid JSON. */
  public static final String INVALID_CURSOR = "INVALID_CURSOR";

  /** Cursor was produced with different filters; reject to prevent inconsistent pages. */
  public static final String INVALID_CURSOR_FILTER_MISMATCH = "INVALID_CURSOR_FILTER_MISMATCH";

  /** Cursor version is not supported. */
  public static final String INVALID_CURSOR_VERSION = "INVALID_CURSOR_VERSION";

  private final String errorCode;

  public InvalidCursorException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public InvalidCursorException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
