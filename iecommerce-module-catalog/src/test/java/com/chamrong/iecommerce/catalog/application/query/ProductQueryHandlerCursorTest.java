package com.chamrong.iecommerce.catalog.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductQueryHandlerCursorTest {

  private static final String TENANT = "tenant-1";

  @Mock private ProductRepositoryPort productRepository;
  @Mock private CatalogCachePort cache;
  @Mock private CatalogMapper mapper;

  private ProductQueryHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ProductQueryHandler(productRepository, cache, mapper);
    TenantContext.setCurrentTenant(TENANT);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  @DisplayName("First page returns CursorPageResponse with data and limit")
  void list_firstPage_returnsCursorPageResponse() {
    when(productRepository.findCursorPage(
            eq(TENANT), eq(null), eq(null), eq(null), eq(null), eq(null), anyInt()))
        .thenReturn(List.of());

    CursorPageResponse<ProductResponse> result = handler.list(null, 20, null, null, null, "en");

    assertThat(result.getData()).isEmpty();
    assertThat(result.getNextCursor()).isNull();
    assertThat(result.isHasNext()).isFalse();
    assertThat(result.getLimit()).isEqualTo(20);
  }

  @Test
  @DisplayName("Cursor with different filterHash throws INVALID_CURSOR_FILTER_MISMATCH")
  void list_cursorFromDifferentFilters_throwsFilterMismatch() {
    Map<String, Object> filtersA = ProductQueryHandler.toFilterMap(ProductStatus.DRAFT, null, null);
    Map<String, Object> filtersB =
        ProductQueryHandler.toFilterMap(ProductStatus.ACTIVE, null, null);
    String hashA = FilterHasher.computeHash(ProductQueryHandler.ENDPOINT_LIST_PRODUCTS, filtersA);
    String hashB = FilterHasher.computeHash(ProductQueryHandler.ENDPOINT_LIST_PRODUCTS, filtersB);
    assertThat(hashA).isNotEqualTo(hashB);

    String cursorForA = CursorCodec.encode(new CursorPayload(1, Instant.now(), "1", hashA));

    assertThatThrownBy(() -> handler.list(cursorForA, 20, ProductStatus.ACTIVE, null, null, "en"))
        .isInstanceOf(InvalidCursorException.class)
        .satisfies(
            e ->
                assertThat(((InvalidCursorException) e).getErrorCode())
                    .isEqualTo(InvalidCursorException.INVALID_CURSOR_FILTER_MISMATCH));
  }
}
