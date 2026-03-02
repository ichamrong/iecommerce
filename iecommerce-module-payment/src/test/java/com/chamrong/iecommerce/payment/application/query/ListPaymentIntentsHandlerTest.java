package com.chamrong.iecommerce.payment.application.query;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ListPaymentIntentsHandlerTest {

  private PaymentIntentRepositoryPort repository;
  private ListPaymentIntentsHandler handler;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(PaymentIntentRepositoryPort.class);
    handler = new ListPaymentIntentsHandler(repository);
  }

  @Test
  void rejectsCursorWithMismatchedFilterHash() {
    String tenantId = "t-1";
    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("tenantId", tenantId);

    // Build a cursor using a different endpoint key so filterHash will mismatch
    String wrongHash = FilterHasher.computeHash("payment:otherEndpoint", filterMap);
    CursorPayload payload =
        new CursorPayload(1, Instant.now(), UUID.randomUUID().toString(), wrongHash);
    String badCursor = CursorCodec.encode(payload);

    assertThrows(InvalidCursorException.class, () -> handler.handle(tenantId, badCursor, 20));

    Mockito.verifyNoInteractions(repository);
  }
}
