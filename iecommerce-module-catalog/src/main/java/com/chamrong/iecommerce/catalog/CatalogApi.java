package com.chamrong.iecommerce.catalog;

import com.chamrong.iecommerce.catalog.domain.Product;
import java.util.List;
import java.util.Optional;

/**
 * Public API of the Catalog module. Other modules must only depend on this interface, never on
 * internal classes like CatalogService.
 */
public interface CatalogApi {
  List<Product> findAllProducts();

  Optional<Product> findProductById(Long id);

  Product createProduct(Product product);
}
