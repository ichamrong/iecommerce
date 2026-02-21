package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.command.AddFacetValueHandler;
import com.chamrong.iecommerce.catalog.application.command.CreateFacetHandler;
import com.chamrong.iecommerce.catalog.application.command.RemoveFacetValueHandler;
import com.chamrong.iecommerce.catalog.application.dto.AddFacetValueRequest;
import com.chamrong.iecommerce.catalog.application.dto.CreateFacetRequest;
import com.chamrong.iecommerce.catalog.application.dto.FacetResponse;
import com.chamrong.iecommerce.catalog.application.query.FacetQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Basic controller for managing facets in the Admin API. Future expansion: Add Facet creation,
 * value management, and translation endpoints.
 */
@Tag(name = "Admin — Facets", description = "Product Facet and Filtering API")
@RestController
@RequestMapping("/api/v1/admin/facets")
@RequiredArgsConstructor
public class FacetController {

  private final FacetQueryHandler facetQueryHandler;
  private final CreateFacetHandler createFacetHandler;
  private final AddFacetValueHandler addFacetValueHandler;
  private final RemoveFacetValueHandler removeFacetValueHandler;

  @Operation(summary = "List all facets", description = "Retrieves all facets for the tenant")
  @GetMapping
  public ResponseEntity<List<FacetResponse>> list(
      @RequestParam(defaultValue = "en") String locale) {
    return ResponseEntity.ok(facetQueryHandler.listFacets(locale));
  }

  @Operation(summary = "Get facet by ID", description = "Retrieves a facet and its values")
  @GetMapping("/{id}")
  public ResponseEntity<FacetResponse> getById(
      @PathVariable Long id, @RequestParam(defaultValue = "en") String locale) {
    return ResponseEntity.ok(facetQueryHandler.findById(id, locale));
  }

  @Operation(summary = "Create facet", description = "Creates a new facet")
  @PostMapping
  public ResponseEntity<FacetResponse> createFacet(@RequestBody CreateFacetRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(createFacetHandler.handle(request));
  }

  @Operation(summary = "Add facet value", description = "Adds a value to an existing facet")
  @PostMapping("/{id}/values")
  public ResponseEntity<FacetResponse> addFacetValue(
      @PathVariable Long id, @RequestBody AddFacetValueRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(addFacetValueHandler.handle(id, request));
  }

  @Operation(summary = "Remove facet value", description = "Removes a value from a facet")
  @DeleteMapping("/{id}/values/{valueId}")
  public ResponseEntity<Void> removeFacetValue(@PathVariable Long id, @PathVariable Long valueId) {
    removeFacetValueHandler.handle(id, valueId);
    return ResponseEntity.noContent().build();
  }
}
