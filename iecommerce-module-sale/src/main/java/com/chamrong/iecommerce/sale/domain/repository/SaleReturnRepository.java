package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.sale.domain.SaleReturn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {
  List<SaleReturn> findByOrderId(String orderId);
}
