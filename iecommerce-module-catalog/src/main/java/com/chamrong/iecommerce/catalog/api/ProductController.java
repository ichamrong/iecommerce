package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.command.AddVariantHandler;
import com.chamrong.iecommerce.catalog.application.command.ArchiveProductHandler;
import com.chamrong.iecommerce.catalog.application.command.CreateProductHandler;
import com.chamrong.iecommerce.catalog.application.command.PublishProductHandler;
import com.chamrong.iecommerce.catalog.application.command.RemoveVariantHandler;
import com.chamrong.iecommerce.catalog.application.command.SetRelationshipsHandler;
import com.chamrong.iecommerce.catalog.application.command.UpdateProductHandler;
import com.chamrong.iecommerce.catalog.application.command.UpdateVariantHandler;
import com.chamrong.iecommerce.catalog.application.command.UpsertProductTranslationHandler;
import com.chamrong.iecommerce.catalog.application.dto.AddVariantRequest;
import com.chamrong.iecommerce.catalog.application.dto.CatalogCursorResponse;
import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest;
import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest.TranslationRequest;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.catalog.application.dto.ProductTranslationsResponse;
import com.chamrong.iecommerce.catalog.application.dto.SetRelationshipsRequest;
import com.chamrong.iecommerce.catalog.application.dto.UpdateProductRequest;
import com.chamrong.iecommerce.catalog.application.dto.UpdateVariantRequest;
import com.chamrong.iecommerce.catalog.application.query.ProductQueryHandler;
import com.chamrong.iecommerce.catalog.domain.ProductStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — Products", description = "Catalog product management (admin)")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductController {

  private final CreateProductHandler createHandler;
  private final UpdateProductHandler updateHandler;
  private final PublishProductHandler publishHandler;
  private final ArchiveProductHandler archiveHandler;
  private final UpsertProductTranslationHandler translationHandler;
  private final ProductQueryHandler queryHandler;
  private final AddVariantHandler addVariantHandler;
  private final UpdateVariantHandler updateVariantHandler;
  private final RemoveVariantHandler removeVariantHandler;
  private final SetRelationshipsHandler setRelationshipsHandler;

  // ── List / Read ────────────────────────────────────────────────────────────

  @Operation(
      summary = "List products",
      description = "Returns all products for the current tenant.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  @GetMapping
  public CatalogCursorResponse<ProductResponse> list(
      @Parameter(description = "Opaque cursor from previous response; omit for first page")
          @RequestParam(required = false)
          String cursor,
      @Parameter(description = "Page size (1–100, default 20)") @RequestParam(defaultValue = "20")
          int limit,
      @Parameter(description = "Filter by status: DRAFT, ACTIVE, ARCHIVED")
          @RequestParam(required = false)
          ProductStatus status,
      @Parameter(description = "Filter by category ID") @RequestParam(required = false)
          Long categoryId,
      @Parameter(description = "Full-text keyword search (uses GIN index)")
          @RequestParam(required = false)
          String keyword,
      @Parameter(description = "Locale for translation (default: en)")
          @RequestParam(defaultValue = "en")
          String locale) {
    return queryHandler.list(cursor, limit, status, categoryId, keyword, locale);
  }

  @Operation(summary = "Get product", description = "Returns a single product by ID.")
  @GetMapping("/{id}")
  public ProductResponse getById(
      @PathVariable Long id, @RequestParam(defaultValue = "en") String locale) {
    return queryHandler.getById(id, locale);
  }

  // ── Create ─────────────────────────────────────────────────────────────────

  @Operation(
      summary = "Create product",
      description = "Creates a new product with translations and variants. Status starts as DRAFT.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Validation error"),
    @ApiResponse(responseCode = "409", description = "Slug conflict")
  })
  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
    var response = createHandler.handle(req);
    return ResponseEntity.created(URI.create("/api/v1/admin/products/" + response.id()))
        .body(response);
  }

  // ── Update ─────────────────────────────────────────────────────────────────

  @Operation(
      summary = "Update product",
      description = "Updates product fields. Null fields are ignored.")
  @PutMapping("/{id}")
  public ProductResponse update(
      @PathVariable Long id,
      @Valid @RequestBody UpdateProductRequest req,
      @RequestParam(defaultValue = "en") String locale) {
    return updateHandler.handle(id, req, locale);
  }

  // ── Lifecycle ──────────────────────────────────────────────────────────────

  @Operation(summary = "Publish product", description = "Transitions DRAFT/ARCHIVED → ACTIVE.")
  @ApiResponse(responseCode = "204", description = "Published")
  @PatchMapping("/{id}/publish")
  public ResponseEntity<Void> publish(@PathVariable Long id) {
    publishHandler.handle(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Archive product", description = "Transitions ACTIVE → ARCHIVED.")
  @ApiResponse(responseCode = "204", description = "Archived")
  @PatchMapping("/{id}/archive")
  public ResponseEntity<Void> archive(@PathVariable Long id) {
    archiveHandler.handle(id);
    return ResponseEntity.noContent().build();
  }

  // ── Translations ───────────────────────────────────────────────────────────

  @Operation(
      summary = "Get all translations",
      description = "Returns all locale entries for a product as a map.")
  @GetMapping("/{id}/translations")
  public ProductTranslationsResponse getAllTranslations(@PathVariable Long id) {
    return queryHandler.getAllTranslations(id);
  }

  @Operation(
      summary = "Upsert translation",
      description = "Creates or updates the translation for one locale.")
  @PutMapping("/{id}/translations/{locale}")
  public ResponseEntity<Void> upsertTranslation(
      @PathVariable Long id, @PathVariable String locale, @RequestBody TranslationRequest req) {
    translationHandler.handle(id, locale, req);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Delete translation",
      description = "Removes a locale entry. Cannot remove 'en'.")
  @ApiResponse(responseCode = "204", description = "Deleted")
  @DeleteMapping("/{id}/translations/{locale}")
  public ResponseEntity<Void> deleteTranslation(
      @PathVariable Long id, @PathVariable String locale) {
    translationHandler.delete(id, locale);
    return ResponseEntity.noContent().build();
  }

  // ── Variants ───────────────────────────────────────────────────────────────

  @Operation(summary = "Add variant", description = "Adds a variant to a product.")
  @PostMapping("/{id}/variants")
  public ResponseEntity<ProductResponse> addVariant(
      @PathVariable Long id,
      @RequestBody AddVariantRequest request,
      @RequestParam(defaultValue = "en") String locale) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(addVariantHandler.handle(id, request, locale));
  }

  @Operation(summary = "Update variant", description = "Updates fields on a specific variant.")
  @PutMapping("/{id}/variants/{variantId}")
  public ResponseEntity<ProductResponse> updateVariant(
      @PathVariable Long id,
      @PathVariable Long variantId,
      @RequestBody UpdateVariantRequest request,
      @RequestParam(defaultValue = "en") String locale) {
    return ResponseEntity.ok(updateVariantHandler.handle(id, variantId, request, locale));
  }

  @Operation(summary = "Remove variant", description = "Removes a variant by ID.")
  @DeleteMapping("/{id}/variants/{variantId}")
  public ResponseEntity<Void> removeVariant(@PathVariable Long id, @PathVariable Long variantId) {
    removeVariantHandler.handle(id, variantId);
    return ResponseEntity.noContent().build();
  }

  // ── Relationships ──────────────────────────────────────────────────────────

  @Operation(
      summary = "Set relationships",
      description = "Replaces the entire list of related products for this product.")
  @PutMapping("/{id}/relationships")
  public ResponseEntity<Void> setRelationships(
      @PathVariable Long id, @RequestBody List<SetRelationshipsRequest> request) {
    setRelationshipsHandler.handle(id, request);
    return ResponseEntity.noContent().build();
  }
}
