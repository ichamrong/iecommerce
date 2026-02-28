package com.chamrong.iecommerce.staff.infrastructure.persistence;

import java.time.Instant;

/** Decoded representation of a cursor token. */
public record StaffCursorDecoded(Instant createdAt, Long id) {}
