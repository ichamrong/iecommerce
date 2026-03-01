package com.chamrong.iecommerce.payment.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.event.PaymentFailedEvent;
import com.chamrong.iecommerce.common.event.PaymentSucceededEvent;
import com.chamrong.iecommerce.payment.application.dto.PaymentRequest;
import com.chamrong.iecommerce.payment.application.dto.PaymentResult;
import com.chamrong.iecommerce.payment.application.spi.PaymentProvider;
import com.chamrong.iecommerce.payment.domain.Payment;
import com.chamrong.iecommerce.payment.domain.PaymentRepository;
import com.chamrong.iecommerce.payment.domain.PaymentStatus;
import com.chamrong.iecommerce.payment.domain.exception.PaymentDomainException;
import com.chamrong.iecommerce.payment.domain.ports.PaymentOutboxPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

  private final PaymentRepository paymentRepository;
  private final List<PaymentProvider> paymentProviders;
  private final PaymentOutboxPort outboxPort;
  private final ObjectMapper objectMapper;

  public PaymentService(
      PaymentRepository paymentRepository,
      List<PaymentProvider> paymentProviders,
      PaymentOutboxPort outboxPort,
      ObjectMapper objectMapper) {
    this.paymentRepository = paymentRepository;
    this.paymentProviders = paymentProviders;
    this.outboxPort = outboxPort;
    this.objectMapper = objectMapper;
  }

  public void saveOutbox(String tenantId, String eventType, Object event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      outboxPort.save(tenantId, eventType, payload);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize payment outbox event type={}", eventType, e);
      throw new IllegalStateException("Cannot serialize event for outbox", e);
    }
  }

  // ── Commands ───────────────────────────────────────────────────────────────

  @Transactional
  public PaymentResult initiate(String tenantId, PaymentRequest req) {
    if (req.idempotencyKey() != null && !req.idempotencyKey().isBlank()) {
      Optional<Payment> existing = paymentRepository.findByIdempotencyKey(req.idempotencyKey());
      if (existing.isPresent()) {
        log.info(
            "Duplicate payment request detected for key={}, returning existing payment",
            req.idempotencyKey());
        return toResponse(existing.get());
      }
    }

    Payment p = new Payment();
    p.setTenantId(tenantId);
    p.setOrderId(req.orderId());
    p.setAmount(new Money(req.amount(), req.currency()));
    p.setMethod(req.method());
    p.setStatus(PaymentStatus.PENDING);
    if (req.idempotencyKey() != null) {
      p.setIdempotencyKey(req.idempotencyKey());
    }

    var provider =
        paymentProviders.stream()
            .filter(pro -> pro.supports(req.method()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Unsupported payment method: " + req.method()));

    String checkoutData =
        provider.initiatePayment(req.orderId().toString(), req.amount(), req.currency());

    p.setCheckoutData(checkoutData);
    log.info(
        "Payment initiated orderId={} method={} amount={}",
        req.orderId(),
        req.method(),
        req.amount());
    return toResponse(paymentRepository.save(p));
  }

  @Transactional
  public PaymentResult markSucceeded(Long paymentId, String externalId) {
    Payment p = require(paymentId);
    p.markSucceeded(externalId);
    Payment saved = paymentRepository.save(p);

    saveOutbox(
        saved.getTenantId(),
        "PaymentSucceededEvent",
        new PaymentSucceededEvent(
            saved.getOrderId(),
            saved.getTenantId(),
            saved.getId(),
            saved.getAmount(),
            saved.getExternalId()));

    log.info("Payment succeeded id={} externalId={}", paymentId, externalId);
    return toResponse(saved);
  }

  @Transactional
  public PaymentResult markFailed(Long paymentId) {
    Payment p = require(paymentId);
    p.markFailed();
    Payment saved = paymentRepository.save(p);

    saveOutbox(
        saved.getTenantId(),
        "PaymentFailedEvent",
        new PaymentFailedEvent(
            saved.getOrderId(), saved.getTenantId(), saved.getId(), "Payment gateway failure"));

    log.warn("Payment failed id={}", paymentId);
    return toResponse(saved);
  }

  @Transactional
  public PaymentResult refund(Long paymentId) {
    Payment p = require(paymentId);
    p.markRefunded();
    log.info("Payment refunded id={}", paymentId);
    return toResponse(paymentRepository.save(p));
  }

  // ── Queries ────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public Optional<PaymentResult> findById(Long id) {
    return paymentRepository.findById(id).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public List<PaymentResult> findByOrderId(Long orderId) {
    return paymentRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private Payment require(Long id) {
    return paymentRepository
        .findById(id)
        .orElseThrow(() -> new PaymentDomainException("Payment not found: " + id));
  }

  private PaymentResult toResponse(Payment p) {
    java.lang.Long id = p.getId();
    return new PaymentResult(id);
  }
}
