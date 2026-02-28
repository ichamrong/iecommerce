package com.chamrong.iecommerce.staff.application.query;

import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffCursorResponse;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.application.util.StaffCursorEncoder;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-model query handler for staff profiles.
 *
 * <p>Depends only on {@link StaffRepositoryPort} — no direct infrastructure imports, preserving the
 * Hexagonal boundary.
 */
@Component
@RequiredArgsConstructor
public class StaffQueryHandler {

  private final StaffRepositoryPort staffRepository;
  private final StaffMapper mapper;

  @Transactional(readOnly = true)
  public StaffResponse findById(Long id) {
    return staffRepository
        .findById(id)
        .map(mapper::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
  }

  /**
   * Cursor-based (keyset) paginated listing.
   *
   * <p>Sort: {@code created_at DESC, id DESC}. Clients pass back {@code nextCursor} from the
   * previous response to advance pages; omit it (null/blank) for the first page.
   */
  @Transactional(readOnly = true)
  public StaffCursorResponse<StaffResponse> findAll(String cursorToken, int limit) {
    var decoded = StaffCursorEncoder.decode(cursorToken);

    // Request limit+1 to detect hasNext without a COUNT query
    List<StaffProfile> rows =
        staffRepository.findNextPage(
            decoded == null ? null : decoded.createdAt(),
            decoded == null ? null : decoded.id(),
            limit + 1);

    boolean hasNext = rows.size() > limit;
    List<StaffProfile> page = hasNext ? rows.subList(0, limit) : rows;

    String nextCursor = null;
    if (hasNext && !page.isEmpty()) {
      StaffProfile last = page.get(page.size() - 1);
      nextCursor = StaffCursorEncoder.encode(last.getCreatedAt(), last.getId());
    }

    return new StaffCursorResponse<>(
        page.stream().map(mapper::toResponse).toList(), nextCursor, hasNext);
  }
}
