package com.chamrong.iecommerce.catalog.infrastructure;

import com.chamrong.iecommerce.catalog.domain.Product;
import com.chamrong.iecommerce.catalog.domain.ProductRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link ProductRepository} port. */
@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long>, ProductRepository {
  @Override
  Optional<Product> findBySlug(String slug);
}
