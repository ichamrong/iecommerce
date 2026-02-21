package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.command.CreateCategoryHandler;
import com.chamrong.iecommerce.catalog.application.command.DeleteCategoryHandler;
import com.chamrong.iecommerce.catalog.application.command.UpdateCategoryHandler;
import com.chamrong.iecommerce.catalog.application.dto.CategoryResponse;
import com.chamrong.iecommerce.catalog.application.dto.CreateCategoryRequest;
import com.chamrong.iecommerce.catalog.application.dto.UpdateCategoryRequest;
import com.chamrong.iecommerce.catalog.application.query.CategoryQueryHandler;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — Categories", description = "Product category tree management")
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryQueryHandler queryHandler;
  private final CreateCategoryHandler createHandler;
  private final UpdateCategoryHandler updateHandler;
  private final DeleteCategoryHandler deleteHandler;

  @Operation(
      summary = "Full category tree",
      description = "Returns all categories as a nested tree for the current tenant.")
  @GetMapping("/tree")
  public List<CategoryResponse> getTree(@RequestParam(defaultValue = "en") String locale) {
    return queryHandler.getTree(locale);
  }

  @Operation(summary = "Get category", description = "Returns a single category by ID.")
  @GetMapping("/{id}")
  public CategoryResponse getById(
      @PathVariable Long id, @RequestParam(defaultValue = "en") String locale) {
    return queryHandler.getById(id, locale);
  }

  @Operation(
      summary = "Category breadcrumb",
      description = "Returns ancestor chain from root to the given category.")
  @GetMapping("/{id}/breadcrumb")
  public List<CategoryResponse> getBreadcrumb(
      @PathVariable Long id, @RequestParam(defaultValue = "en") String locale) {
    return queryHandler.getBreadcrumb(id, locale);
  }

  @Operation(summary = "Create category", description = "Creates a new category in the tree.")
  @PostMapping
  public ResponseEntity<CategoryResponse> createCategory(
      @RequestBody CreateCategoryRequest request) {
    var response = createHandler.handle(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "Update category",
      description = "Updates category fields, including parent move.")
  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponse> updateCategory(
      @PathVariable Long id,
      @RequestBody UpdateCategoryRequest request,
      @RequestParam(defaultValue = "en") String locale) {
    return ResponseEntity.ok(updateHandler.handle(id, request, locale));
  }

  @Operation(summary = "Delete category", description = "Deletes a category from the tree.")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    deleteHandler.handle(id);
    return ResponseEntity.noContent().build();
  }
}
