package com.chamrong.iecommerce.common.pagination;

import java.time.Instant;
import java.util.Objects;

/**
 * Decoded cursor content for keyset pagination.
 *
 * <p>{@code v} must be a supported version (currently 1). {@code filterHash} is compared on decode
 * to reject cursors from a different filter set.
 */
public final class CursorPayload {

  private static final int SUPPORTED_VERSION = 1;

  private final int v;
  private final Instant createdAt;
  private final String id;
  private final String filterHash;

  public CursorPayload(int v, Instant createdAt, String id, String filterHash) {
    this.v = v;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.id = Objects.requireNonNull(id, "id");
    this.filterHash = filterHash != null ? filterHash : "";
  }

  /** Supported schema version. */
  public static int getSupportedVersion() {
    return SUPPORTED_VERSION;
  }

  public int getV() {
    return v;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getId() {
    return id;
  }

  public String getFilterHash() {
    return filterHash;
  }

  /** Validates that {@code v} is supported; throws if not. */
  public void validateVersion() {
    if (v != SUPPORTED_VERSION) {
      throw new InvalidCursorException(
          InvalidCursorException.INVALID_CURSOR_VERSION,
          "Unsupported cursor version: " + v + ", supported: " + SUPPORTED_VERSION);
    }
  }
}
