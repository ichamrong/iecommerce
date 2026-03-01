package com.chamrong.iecommerce.payment.application.query;

import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handler for listing payment intents with keyset pagination. */
@Component
@RequiredArgsConstructor
public class ListPaymentIntentsHandler {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(ListPaymentIntentsHandler.class);

  private final PaymentIntentRepositoryPort repository;

  public List<PaymentIntent> handle(Query query) {
    log.info(
        "Listing payment intents for tenant={} cursorTime={} cursorId={} limit={}",
        query.tenantId(),
        query.cursorTime(),
        query.cursorId(),
        query.limit());

    return repository.findNextPage(
        query.tenantId(), query.cursorTime(), query.cursorId(), query.limit());
  }

  public record Query(String tenantId, Instant cursorTime, UUID cursorId, int limit) {}
}
