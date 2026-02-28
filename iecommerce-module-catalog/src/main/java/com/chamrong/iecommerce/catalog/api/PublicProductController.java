package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.dto.CatalogCursorResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.query.ProductQueryHandler;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public (unauthenticated) read-only product endpoints for the storefront. Only returns ACTIVE
 * products.
 */
@Tag(name = "Public — Products", description = "Public product catalog (storefront)")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class PublicProductController {

  private final ProductQueryHandler queryHandler;

  @Operation(
      summary = "List active products",
      description = "Returns published products (cursor-paginated).")
  @GetMapping
  public CatalogCursorResponse<ProductResponse> list(
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "en") String locale) {
    return queryHandler.list(cursor, limit, ProductStatus.ACTIVE, null, null, locale);
  }

  @Operation(
      summary = "Get product by slug",
      description = "Returns a single active product by URL slug.")
  @GetMapping("/{slug}")
  public ProductResponse getBySlug(
      @PathVariable String slug, @RequestParam(defaultValue = "en") String locale) {
    return queryHandler.getBySlug(slug, locale);
  }
}
