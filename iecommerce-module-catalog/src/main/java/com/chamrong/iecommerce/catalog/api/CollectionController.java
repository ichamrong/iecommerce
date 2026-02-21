package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.command.AddProductToCollectionHandler;
import com.chamrong.iecommerce.catalog.application.command.CreateCollectionHandler;
import com.chamrong.iecommerce.catalog.application.command.RemoveProductFromCollectionHandler;
import com.chamrong.iecommerce.catalog.application.command.UpdateCollectionHandler;
import com.chamrong.iecommerce.catalog.application.dto.CollectionResponse;
import com.chamrong.iecommerce.catalog.application.dto.CreateCollectionRequest;
import com.chamrong.iecommerce.catalog.application.dto.UpdateCollectionRequest;
import com.chamrong.iecommerce.catalog.application.query.CollectionQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — Collections", description = "Product Collection Management")
@RestController
@RequestMapping("/api/v1/admin/collections")
@RequiredArgsConstructor
public class CollectionController {

  private final CreateCollectionHandler createCollectionHandler;
  private final UpdateCollectionHandler updateCollectionHandler;
  private final AddProductToCollectionHandler addProductToCollectionHandler;
  private final RemoveProductFromCollectionHandler removeProductFromCollectionHandler;
  private final CollectionQueryHandler collectionQueryHandler;

  @Operation(summary = "List collections")
  @GetMapping
  public ResponseEntity<Page<CollectionResponse>> listCollections(
      @RequestParam(defaultValue = "en") String locale, Pageable pageable) {
    return ResponseEntity.ok(collectionQueryHandler.listCollections(locale, pageable));
  }

  @Operation(summary = "Get Collection by ID")
  @GetMapping("/{id}")
  public ResponseEntity<CollectionResponse> getCollection(
      @PathVariable Long id, @RequestParam(defaultValue = "en") String locale) {
    return ResponseEntity.ok(collectionQueryHandler.findById(id, locale));
  }

  @Operation(summary = "Create collection")
  @PostMapping
  public ResponseEntity<CollectionResponse> createCollection(
      @RequestBody CreateCollectionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(createCollectionHandler.handle(request));
  }

  @Operation(summary = "Update collection")
  @PutMapping("/{id}")
  public ResponseEntity<CollectionResponse> updateCollection(
      @PathVariable Long id, @RequestBody UpdateCollectionRequest request) {
    return ResponseEntity.ok(updateCollectionHandler.handle(id, request));
  }

  @Operation(summary = "Add product to collection")
  @PostMapping("/{id}/products/{productId}")
  public ResponseEntity<Void> addProductToCollection(
      @PathVariable Long id, @PathVariable Long productId) {
    addProductToCollectionHandler.handle(id, productId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "Remove product from collection")
  @DeleteMapping("/{id}/products/{productId}")
  public ResponseEntity<Void> removeProductFromCollection(
      @PathVariable Long id, @PathVariable Long productId) {
    removeProductFromCollectionHandler.handle(id, productId);
    return ResponseEntity.noContent().build();
  }
}
