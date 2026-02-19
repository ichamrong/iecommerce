package com.chamrong.iecommerce.catalog.infrastructure;

import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductRepository implements ProductRepository {

  private final ProductJpaInterface jpaInterface;

  public JpaProductRepository(ProductJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public List<Product> findAll() {
    return jpaInterface.findAll();
  }

  @Override
  public Optional<Product> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public Optional<Product> findBySlug(String slug) {
    return jpaInterface.findBySlug(slug);
  }

  @Override
  public Product save(Product product) {
    return jpaInterface.save(product);
  }

  public interface ProductJpaInterface extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
  }
}
