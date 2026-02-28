package com.chamrong.iecommerce.staff.application.dto;

import java.util.List;

/**
 * Generic cursor-paginated response envelope.
 *
 * @param <T> element type
 */
public record StaffCursorResponse<T>(List<T> data, String nextCursor, boolean hasNext) {}
