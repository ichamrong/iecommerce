package com.chamrong.iecommerce.common.outbox;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public abstract class AbstractOutboxRelay<E extends BaseOutboxEvent> {

  private static final Logger log = LoggerFactory.getLogger(AbstractOutboxRelay.class);

  private final EventDispatcher eventDispatcher;
  private final ObjectMapper objectMapper;

  @Transactional
  public void processPendingEvents(List<E> events) {
    if (events.isEmpty()) return;

    log.debug("Outbox relay: processing {} pending event(s)", events.size());

    for (E outboxEvent : events) {
      try {
        Class<?> eventClass = getEventClass(outboxEvent.getEventType());
        var payload = objectMapper.readValue(outboxEvent.getPayload(), eventClass);
        eventDispatcher.dispatch(payload);

        outboxEvent.markSent();
        saveEvent(outboxEvent);

        log.info(
            "Outbox relay: delivered eventType={} id={}",
            outboxEvent.getEventType(),
            outboxEvent.getId());

      } catch (Exception ex) {
        outboxEvent.markFailed();
        saveEvent(outboxEvent);
        log.error(
            "Outbox relay: FAILED to deliver eventType={} id={}",
            outboxEvent.getEventType(),
            outboxEvent.getId(),
            ex);
      }
    }
  }

  protected abstract Class<?> getEventClass(String eventType);

  protected abstract void saveEvent(E event);
}
