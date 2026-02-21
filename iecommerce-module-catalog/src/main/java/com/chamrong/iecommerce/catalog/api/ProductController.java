package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.CatalogService;
import com.chamrong.iecommerce.catalog.domain.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Product catalog management")
@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final CatalogService catalogService;

  public ProductController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @Operation(summary = "List all products", description = "Returns all products in the catalog.")
  @GetMapping
  public List<Product> getAllProducts() {
    return catalogService.findAllProducts();
  }

  @Operation(summary = "Create product", description = "Adds a new product to the catalog.")
  @PostMapping
  public Product createProduct(@RequestBody Product product) {
    return catalogService.createProduct(product);
  }
}
