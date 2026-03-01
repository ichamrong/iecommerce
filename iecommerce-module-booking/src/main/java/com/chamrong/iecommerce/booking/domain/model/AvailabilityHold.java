package com.chamrong.iecommerce.booking.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Temporary hold on availability for N minutes. Anti-race: prevents double-booking during create
 * flow.
 *
 * @param id              hold id
 * @param tenantId        tenant
 * @param resourceProductId product/resource id
 * @param resourceVariantId optional variant
 * @param resourceId      for appointment: staff/room id
 * @param startAt         start of window
 * @param endAt           end of window
 * @param expiresAt       when hold expires (auto-release)
 * @param holdToken       unique token for idempotency
 * @param idempotencyKey  client idempotency key
 */
public record AvailabilityHold(
    Long id,
    String tenantId,
    Long resourceProductId,
    Long resourceVariantId,
    Long resourceId,
    Instant startAt,
    Instant endAt,
    Instant expiresAt,
    String holdToken,
    String idempotencyKey) {

  public AvailabilityHold {
    Objects.requireNonNull(tenantId, "tenantId");
    Objects.requireNonNull(startAt, "startAt");
    Objects.requireNonNull(endAt, "endAt");
    Objects.requireNonNull(expiresAt, "expiresAt");
    Objects.requireNonNull(holdToken, "holdToken");
  }

  public boolean isExpired(Instant now) {
    return now.isAfter(expiresAt);
  }
}
