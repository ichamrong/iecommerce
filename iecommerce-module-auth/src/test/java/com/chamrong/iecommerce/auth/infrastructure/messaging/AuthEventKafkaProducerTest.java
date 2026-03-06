package com.chamrong.iecommerce.auth.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/** Unit tests for {@link AuthEventKafkaProducer}. */
@ExtendWith(MockitoExtension.class)
class AuthEventKafkaProducerTest {

  @Mock private KafkaTemplate<String, String> kafkaTemplate;
  @Mock private ObjectMapper objectMapper;

  private AuthEventKafkaProducer producer;

  @BeforeEach
  void setUp() {
    producer = new AuthEventKafkaProducer(kafkaTemplate, objectMapper);
  }

  @Test
  void publishEventShouldSerializePayloadAndSendToAuthEventsTopic() throws Exception {
    Object payload = new TestEvent("value");
    when(objectMapper.writeValueAsString(payload)).thenReturn("{\"field\":\"value\"}");

    CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
    SendResult<String, String> result =
        new SendResult<>(
            null,
            new RecordMetadata(new TopicPartition(KafkaTopics.AUTH_EVENTS, 0), 0, 0, 0, 0L, 0, 0));
    future.complete(result);

    when(kafkaTemplate.send(eq(KafkaTopics.AUTH_EVENTS), eq("TENANT-1"), any())).thenReturn(future);

    assertThatCode(() -> producer.publishEvent("TestEvent", payload, "TENANT-1"))
        .doesNotThrowAnyException();

    verify(objectMapper).writeValueAsString(payload);
    verify(kafkaTemplate).send(KafkaTopics.AUTH_EVENTS, "TENANT-1", "{\"field\":\"value\"}");
  }

  @Test
  void publishShouldSwallowJsonProcessingExceptions() throws Exception {
    Object payload = new TestEvent("value");
    when(objectMapper.writeValueAsString(payload))
        .thenThrow(new JsonProcessingException("boom") {});

    assertThatCode(() -> producer.publishEvent("TestEvent", payload, "TENANT-1"))
        .doesNotThrowAnyException();
  }

  private record TestEvent(String field) {}
}
