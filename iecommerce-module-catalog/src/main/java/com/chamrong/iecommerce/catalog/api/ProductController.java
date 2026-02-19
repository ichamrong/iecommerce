package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.catalog.application.CatalogService;
import com.chamrong.iecommerce.catalog.domain.Product;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final CatalogService catalogService;

  public ProductController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping
  public List<Product> getAllProducts() {
    return catalogService.findAllProducts();
  }

  @PostMapping
  public Product createProduct(@RequestBody Product product) {
    return catalogService.createProduct(product);
  }
}
