package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.domain.AuditEvent;
import com.chamrong.iecommerce.audit.domain.AuditRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaAuditRepository implements AuditRepository {

  private final AuditJpaInterface jpaInterface;

  public JpaAuditRepository(AuditJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public AuditEvent save(AuditEvent event) {
    return jpaInterface.save(event);
  }

  @Override
  public Optional<AuditEvent> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public List<AuditEvent> findByUserId(String userId) {
    return jpaInterface.findByUserId(userId);
  }

  public interface AuditJpaInterface extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByUserId(String userId);
  }
}
