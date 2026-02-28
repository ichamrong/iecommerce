package com.chamrong.iecommerce.customer.domain;

import com.chamrong.iecommerce.customer.api.util.CursorEncoder.Cursor;
import java.util.List;

public interface CustomerRepositoryCustom {
  List<Customer> findNextPage(Cursor cursor, int limit);
}
