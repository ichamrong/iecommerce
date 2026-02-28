package com.chamrong.iecommerce.customer.api.dto;

import java.util.List;

public record CursorResponse<T>(List<T> data, String nextCursor, boolean hasNext) {}
