package com.chamrong.iecommerce.customer.application.query;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.customer.application.dto.CustomerFilters;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerQueryHandler {

  public static final String ENDPOINT_LIST_CUSTOMERS = "customer:listCustomers";

  private final CustomerRepositoryPort customerRepository;
  private final com.chamrong.iecommerce.customer.application.CustomerMapper mapper;

  public CustomerQueryHandler(
      CustomerRepositoryPort customerRepository,
      com.chamrong.iecommerce.customer.application.CustomerMapper mapper) {
    this.customerRepository = customerRepository;
    this.mapper = mapper;
  }

  public CustomerResponse findById(String tenantId, Long id) {
    return customerRepository
        .findById(id)
        .map(
            c -> {
              TenantGuard.requireSameTenant(c.getTenantId(), tenantId);
              return mapper.toResponse(c);
            })
        .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
  }

  public CustomerResponse findByAuthUserId(String tenantId, Long authUserId) {
    return customerRepository
        .findByAuthUserId(authUserId)
        .map(
            c -> {
              TenantGuard.requireSameTenant(c.getTenantId(), tenantId);
              return mapper.toResponse(c);
            })
        .orElseThrow(
            () ->
                new EntityNotFoundException("Customer not found for auth user id: " + authUserId));
  }

  /** Returns all customers for tenant. Prefer {@link #findPage} for cursor pagination. */
  public List<CustomerResponse> findAll(String tenantId) {
    return customerRepository.findAllByTenantId(tenantId).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  public CursorPageResponse<CustomerResponse> findPage(
      String tenantId, CustomerFilters filters, String cursor, int limit) {
    int effectiveLimit = Math.min(100, Math.max(1, limit));
    int limitPlusOne = effectiveLimit + 1;
    Map<String, Object> filterMap = toFilterMap(filters);
    filterMap.put("tenantId", tenantId);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_CUSTOMERS, filterMap);

    Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<Customer> list =
        customerRepository.findCursorPage(tenantId, cursorCreatedAt, cursorId, limitPlusOne);

    boolean hasNext = list.size() == limitPlusOne;
    List<Customer> pageData = hasNext ? list.subList(0, effectiveLimit) : list;
    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      Customer last = pageData.get(pageData.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }
    List<CustomerResponse> data = pageData.stream().map(mapper::toResponse).toList();
    return CursorPageResponse.of(data, nextCursor, hasNext, effectiveLimit);
  }

  private static Map<String, Object> toFilterMap(CustomerFilters f) {
    Map<String, Object> m = new LinkedHashMap<>();
    if (f != null && f.status() != null) m.put("status", f.status().name());
    if (f != null && f.search() != null && !f.search().isBlank()) m.put("search", f.search());
    if (f != null && f.createdAtFrom() != null) m.put("createdAtFrom", f.createdAtFrom());
    if (f != null && f.createdAtTo() != null) m.put("createdAtTo", f.createdAtTo());
    return m;
  }
}
