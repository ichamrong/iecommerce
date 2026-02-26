package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.sale.domain.Quotation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {
  List<Quotation> findByCustomerIdAndTenantId(String customerId, String tenantId);
}
