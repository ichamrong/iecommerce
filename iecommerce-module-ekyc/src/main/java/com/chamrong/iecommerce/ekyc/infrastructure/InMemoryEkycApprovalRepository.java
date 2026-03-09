package com.chamrong.iecommerce.ekyc.infrastructure;

import com.chamrong.iecommerce.ekyc.domain.EkycApproval;
import com.chamrong.iecommerce.ekyc.domain.EkycStatus;
import com.chamrong.iecommerce.ekyc.domain.RiskScore;
import com.chamrong.iecommerce.ekyc.domain.ports.EkycApprovalRepositoryPort;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** In-memory implementation of eKYC approval repository. For production, replace with JPA. */
@Component
public class InMemoryEkycApprovalRepository implements EkycApprovalRepositoryPort {

  private final Map<String, EkycApproval> store = new ConcurrentHashMap<>();
  private final AtomicInteger counter = new AtomicInteger(0);

  @PostConstruct
  void seedSample() {
    if (store.isEmpty()) {
      EkycApproval a =
          EkycApproval.builder()
              .id("ekyc-1")
              .tenantId("tenant-acme")
              .tenantName("Acme Store")
              .ownerName("John Doe")
              .documentType("National ID")
              .documentUrl("https://example.com/doc1.pdf")
              .submittedAt(Instant.now())
              .status(EkycStatus.PENDING)
              .riskScore(RiskScore.Low)
              .build();
      store.put(a.getId(), a);
    }
  }

  @Override
  public List<EkycApproval> findAll(
      EkycStatus status, RiskScore riskScore, int page, int pageSize) {
    List<EkycApproval> filtered =
        store.values().stream()
            .filter(a -> status == null || a.getStatus() == status)
            .filter(a -> riskScore == null || a.getRiskScore() == riskScore)
            .sorted((x, y) -> x.getSubmittedAt().compareTo(y.getSubmittedAt()))
            .toList();
    int from = Math.min((page - 1) * pageSize, filtered.size());
    int to = Math.min(from + pageSize, filtered.size());
    return from < to ? filtered.subList(from, to) : List.of();
  }

  @Override
  public int countAll(EkycStatus status, RiskScore riskScore) {
    return (int)
        store.values().stream()
            .filter(a -> status == null || a.getStatus() == status)
            .filter(a -> riskScore == null || a.getRiskScore() == riskScore)
            .count();
  }

  @Override
  public Optional<EkycApproval> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public EkycApproval save(EkycApproval approval) {
    if (approval.getId() == null || approval.getId().isBlank()) {
      approval.setId("ekyc-" + counter.incrementAndGet());
    }
    store.put(approval.getId(), approval);
    return approval;
  }
}
