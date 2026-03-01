/**
 * Shared cursor pagination standard for keyset-based list endpoints.
 *
 * <p>All list APIs use: sort {@code created_at DESC, id DESC}; cursor as Base64URL(JSON) with
 * {@code v}, {@code createdAt}, {@code id}, {@code filterHash}; reject cursor when filterHash
 * mismatches. Response shape: {@code data}, {@code nextCursor}, {@code hasNext}, {@code limit}.
 *
 * <p>Domain-agnostic: no Spring types in public API of {@link
 * com.chamrong.iecommerce.common.pagination.CursorPayload} or {@link
 * com.chamrong.iecommerce.common.pagination.CursorPageRequest}.
 */
@org.springframework.modulith.NamedInterface("pagination")
@org.springframework.lang.NonNullApi
@org.springframework.lang.NonNullFields
package com.chamrong.iecommerce.common.pagination;
