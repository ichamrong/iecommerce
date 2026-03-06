package com.chamrong.iecommerce.catalog.application.query;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.catalog.application.CatalogMapper;
import com.chamrong.iecommerce.catalog.domain.CatalogCachePort;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import com.chamrong.iecommerce.catalog.domain.ProductType;
import com.chamrong.iecommerce.catalog.domain.ports.ProductRepositoryPort;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductQueryHandlerMultiTenantTest {

  private static final String TENANT_A = "tenant-a";
  private static final String TENANT_B = "tenant-b";

  @Mock private ProductRepositoryPort productRepository;
  @Mock private CatalogCachePort cache;
  @Mock private CatalogMapper mapper;

  private ProductQueryHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ProductQueryHandler(productRepository, cache, mapper);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  @DisplayName("getById denies access when product belongs to another tenant")
  void getById_deniesAccessForOtherTenant() {
    Product product =
        new Product(TENANT_A, "product-a", ProductType.PHYSICAL, Money.of(BigDecimal.TEN, "USD"));
    product.setId(1L);

    when(cache.getProduct(1L)).thenReturn(Optional.empty());
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    TenantContext.setCurrentTenant(TENANT_B);

    assertThatThrownBy(() -> handler.getById(1L, "en")).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  @DisplayName("getBySlug only returns products for current tenant")
  void getBySlug_respectsTenantScope() {
    Product product =
        new Product(TENANT_A, "slug-a", ProductType.PHYSICAL, Money.of(BigDecimal.ONE, "USD"));
    product.setId(2L);

    // Force ACTIVE status without exposing a public setter (test-only reflection)
    try {
      Field statusField = Product.class.getDeclaredField("status");
      statusField.setAccessible(true);
      statusField.set(product, ProductStatus.ACTIVE);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    when(cache.getProductBySlug(TENANT_A, "slug-a")).thenReturn(Optional.empty());
    when(productRepository.findByTenantIdAndSlug(TENANT_A, "slug-a"))
        .thenReturn(Optional.of(product));

    TenantContext.setCurrentTenant(TENANT_A);

    // Should not throw and should delegate to mapper; mapper behavior is not asserted here
    handler.getBySlug("slug-a", "en");
  }

  @Test
  @DisplayName("getBySku is tenant-scoped via repository")
  void getBySku_respectsTenantScope() {
    Product product =
        new Product(TENANT_A, "sku-product", ProductType.PHYSICAL, Money.of(BigDecimal.ONE, "USD"));
    product.setId(3L);

    when(productRepository.findByTenantIdAndVariantSku(TENANT_A, "SKU-1"))
        .thenReturn(Optional.of(product));

    TenantContext.setCurrentTenant(TENANT_A);

    handler.getBySku("SKU-1", "en");
  }

  @Test
  @DisplayName("getByBarcode is tenant-scoped via repository")
  void getByBarcode_respectsTenantScope() {
    Product product =
        new Product(
            TENANT_A, "barcode-product", ProductType.PHYSICAL, Money.of(BigDecimal.ONE, "USD"));
    product.setId(4L);

    when(productRepository.findByTenantIdAndBarcode(TENANT_A, "BAR-1"))
        .thenReturn(Optional.of(product));

    TenantContext.setCurrentTenant(TENANT_A);

    handler.getByBarcode("BAR-1", "en");
  }

  @Test
  @DisplayName("getById throws when product not found")
  void getById_throwsWhenProductMissing() {
    when(cache.getProduct(99L)).thenReturn(Optional.empty());
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    TenantContext.setCurrentTenant(TENANT_A);

    assertThatThrownBy(() -> handler.getById(99L, "en"))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
