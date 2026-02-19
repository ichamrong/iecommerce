package com.chamrong.iecommerce.catalog.application;

import com.chamrong.iecommerce.catalog.CatalogApi;
import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService implements CatalogApi {

  private final ProductRepository productRepository;

  public CatalogService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public List<Product> findAllProducts() {
    return productRepository.findAll();
  }

  public Optional<Product> findProductById(Long id) {
    return productRepository.findById(id);
  }

  @Transactional
  public Product createProduct(Product product) {
    return productRepository.save(product);
  }
}
