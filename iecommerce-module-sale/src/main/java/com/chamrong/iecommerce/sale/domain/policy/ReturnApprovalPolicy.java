package com.chamrong.iecommerce.sale.domain.policy;

import com.chamrong.iecommerce.sale.domain.model.SaleReturn;
import java.math.BigDecimal;
import lombok.Value;

@Value
public class ReturnApprovalPolicy {

  private static final BigDecimal SUPERVISOR_THRESHOLD = new BigDecimal("500.00");

  // The original method was named requiresSupervisorApproval.
  // The instruction implies renaming it to isSatisfiedBy and adding @Override,
  // which means this class should implement an interface.
  // However, without the interface definition, adding @Override would cause a compilation error.
  // Assuming the intent is to rename the method and use the existing SUPERVISOR_THRESHOLD.
  // The instruction also mentions "Fix totalRefundAmount usage", but totalRefundAmount is not in
  // the original code.
  // The closest is getTotalAmount().
  // Given the snippet, it seems the method `requiresSupervisorApproval` is being replaced by
  // `isSatisfiedBy`.
  // The `threshold` variable in the snippet should be `SUPERVISOR_THRESHOLD`.
  // The `@Override` annotation is problematic without an interface. I will omit it to keep the code
  // syntactically correct.
  public boolean isSatisfiedBy(SaleReturn saleReturn) {
    return saleReturn.getTotalAmount().getAmount().compareTo(SUPERVISOR_THRESHOLD) > 0;
  }

  public boolean isEligible(SaleReturn saleReturn, boolean isSupervisor) {
    // This method previously called requiresSupervisorApproval.
    // It should now call the new isSatisfiedBy method.
    if (isSatisfiedBy(saleReturn)) {
      return isSupervisor;
    }
    return true;
  }
}
