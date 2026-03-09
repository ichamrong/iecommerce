package com.chamrong.iecommerce.ekyc.domain.ports;

import com.chamrong.iecommerce.ekyc.domain.EkycApproval;
import com.chamrong.iecommerce.ekyc.domain.EkycStatus;
import com.chamrong.iecommerce.ekyc.domain.RiskScore;
import java.util.List;
import java.util.Optional;

/** Port for eKYC approval persistence. */
public interface EkycApprovalRepositoryPort {

  List<EkycApproval> findAll(EkycStatus status, RiskScore riskScore, int page, int pageSize);

  int countAll(EkycStatus status, RiskScore riskScore);

  Optional<EkycApproval> findById(String id);

  EkycApproval save(EkycApproval approval);
}
