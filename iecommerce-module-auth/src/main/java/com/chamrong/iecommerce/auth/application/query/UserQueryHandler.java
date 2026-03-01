package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Query handler for user list and get-by-id. Uses cursor pagination for list. */
@Component
public class UserQueryHandler {

  private static final String LIST_USERS_ENDPOINT = "auth:listUsers";

  private final UserRepositoryPort userRepository;

  public UserQueryHandler(UserRepositoryPort userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * List users in the current tenant with cursor pagination.
   *
   * @param tenantId from TenantContext (caller must pass)
   * @param cursor optional cursor for next page
   * @param limit page size (clamped 1..100)
   * @param filters optional filters for filterHash binding
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<User> listUsers(
      String tenantId, String cursor, int limit, Map<String, Object> filters) {
    if (tenantId == null || tenantId.isBlank()) {
      return CursorPageResponse.lastPage(List.of(), Math.min(100, Math.max(1, limit)));
    }
    String filterHash =
        FilterHasher.computeHash(LIST_USERS_ENDPOINT, filters != null ? filters : Map.of());
    java.time.Instant cursorCreatedAt = null;
    Long cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursor, filterHash);
      cursorCreatedAt = payload.getCreatedAt();
      try {
        cursorId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new com.chamrong.iecommerce.common.pagination.InvalidCursorException(
            com.chamrong.iecommerce.common.pagination.InvalidCursorException.INVALID_CURSOR,
            "Invalid cursor id");
      }
    }
    int limitPlusOne = Math.min(100, Math.max(1, limit)) + 1;
    List<User> list = userRepository.findPage(tenantId, cursorCreatedAt, cursorId, limitPlusOne);
    int effectiveLimit = Math.min(100, Math.max(1, limit));
    boolean hasNext = list.size() > effectiveLimit;
    List<User> pageData = hasNext ? list.subList(0, effectiveLimit) : list;
    String nextCursor = null;
    if (hasNext && !pageData.isEmpty()) {
      User last = pageData.get(pageData.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(1, last.getCreatedAt(), String.valueOf(last.getId()), filterHash));
    }
    return CursorPageResponse.of(pageData, nextCursor, hasNext, effectiveLimit);
  }

  public Optional<User> findUserById(Long id) {
    return userRepository.findById(id);
  }
}
