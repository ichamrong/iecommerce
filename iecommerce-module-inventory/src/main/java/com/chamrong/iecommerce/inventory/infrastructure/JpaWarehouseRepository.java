package com.chamrong.iecommerce.inventory.infrastructure;

import com.chamrong.iecommerce.inventory.domain.Warehouse;
import com.chamrong.iecommerce.inventory.domain.WarehouseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaWarehouseRepository implements WarehouseRepository {

  private final WarehouseJpaInterface jpaInterface;

  public JpaWarehouseRepository(WarehouseJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Warehouse save(Warehouse warehouse) {
    return jpaInterface.save(warehouse);
  }

  @Override
  public Optional<Warehouse> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public List<Warehouse> findAll() {
    return jpaInterface.findAll();
  }

  public interface WarehouseJpaInterface extends JpaRepository<Warehouse, Long> {}
}
