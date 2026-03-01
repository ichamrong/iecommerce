package com.chamrong.iecommerce.invoice.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.invoice.application.command.CreateInvoiceDraftCommand;
import com.chamrong.iecommerce.invoice.application.command.IssueInvoiceCommand;
import com.chamrong.iecommerce.invoice.application.command.MarkInvoicePaidCommand;
import com.chamrong.iecommerce.invoice.application.command.VoidInvoiceCommand;
import com.chamrong.iecommerce.invoice.application.dto.AuditEntryResponse;
import com.chamrong.iecommerce.invoice.application.dto.CursorPageResponse;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceDetailResponse;
import com.chamrong.iecommerce.invoice.application.dto.SignatureVerificationResponse;
import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry;
import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry.Action;
import com.chamrong.iecommerce.invoice.domain.InvoiceLine;
import com.chamrong.iecommerce.invoice.domain.InvoiceLogEvents;
import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;
import com.chamrong.iecommerce.invoice.domain.exception.InvoiceNotFoundException;
import com.chamrong.iecommerce.invoice.domain.ports.ClockPort;
import com.chamrong.iecommerce.invoice.domain.ports.DigitalSignaturePort;
import com.chamrong.iecommerce.invoice.domain.ports.DigitalSignaturePort.SignResult;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceAuditRepositoryPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceEmailDeliveryPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceNumberGeneratorPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceOutboxPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoicePdfRendererPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceRepositoryPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceSignatureRepositoryPort;
import com.chamrong.iecommerce.invoice.infrastructure.email.InvoiceEmailDelivery;
import com.chamrong.iecommerce.invoice.infrastructure.email.InvoiceEmailDelivery.MessageType;
import com.chamrong.iecommerce.invoice.infrastructure.email.InvoiceEmailDelivery.Status;
import com.chamrong.iecommerce.invoice.infrastructure.security.InvoiceCanonicalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-layer facade for the Invoice bounded context.
 *
 * <p>Orchestrates domain operations: aggregate manipulation, signing, audit logging, and outbox
 * event publishing — all within a single transaction per use case.
 *
 * <p>ASVS V8.3 — Business logic protection: all mutations enforce status invariants through
 * aggregate domain methods; never bypassing the aggregate.
 *
 * <p>No Spring Security annotations here — authorization is enforced at the API layer before
 * reaching this service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceApplicationService {

  private static final int MAX_PAGE_SIZE = 100;

  private final InvoiceRepositoryPort invoiceRepository;
  private final InvoiceSignatureRepositoryPort signatureRepository;
  private final InvoiceAuditRepositoryPort auditRepository;
  private final InvoiceOutboxPort outboxPort;
  private final InvoiceNumberGeneratorPort numberGenerator;
  private final DigitalSignaturePort signaturePort;
  private final InvoicePdfRendererPort pdfRenderer;
  private final InvoiceCanonicalizer canonicalizer;
  private final ClockPort clock;
  private final InvoiceMapper mapper;
  private final InvoiceEmailDeliveryPort emailDeliveryPort;

  // ── Commands ──────────────────────────────────────────────────────────────

  /**
   * Creates a new DRAFT invoice with the given line items.
   *
   * @param cmd the create command
   * @return the persisted draft response
   */
  @Transactional
  public InvoiceDetailResponse createDraft(CreateInvoiceDraftCommand cmd) {
    Invoice invoice =
        Invoice.createDraft(
            cmd.tenantId(),
            cmd.orderId(),
            cmd.customerId(),
            cmd.currency(),
            cmd.dueDate(),
            cmd.sellerSnapshot(),
            cmd.buyerSnapshot());

    for (int i = 0; i < cmd.lines().size(); i++) {
      CreateInvoiceDraftCommand.LineItem item = cmd.lines().get(i);
      InvoiceLine line =
          InvoiceLine.of(
              item.sku(),
              item.productName(),
              item.description(),
              item.quantity(),
              new Money(item.unitPriceAmount(), cmd.currency()),
              item.taxRate(),
              item.lineOrder());
      invoice.addLine(line);
    }

    Invoice saved = invoiceRepository.save(invoice);

    auditRepository.append(
        InvoiceAuditEntry.of(
            saved.getId(), cmd.tenantId(), Action.CREATED, cmd.actorId(), null, clock.now()));

    log.info(
        "Invoice DRAFT created: id={}, tenant={}, actor={}",
        saved.getId(),
        cmd.tenantId(),
        cmd.actorId());

    return mapper.toDetailResponse(saved, null);
  }

  /**
   * Issues a DRAFT invoice: assigns invoice number, computes canonical hash, signs, stores
   * signature, emits outbox event.
   *
   * <p>This is the most critical use case — all steps occur atomically within a single transaction.
   *
   * @param cmd the issue command
   * @return the updated invoice with signature metadata
   * @throws InvoiceNotFoundException if the invoice doesn't exist for this tenant
   */
  @Transactional
  public InvoiceDetailResponse issueInvoice(IssueInvoiceCommand cmd) {
    Invoice invoice = requireByIdAndTenant(cmd.invoiceId(), cmd.tenantId());

    Instant now = clock.now();
    int year = LocalDate.ofInstant(now, ZoneOffset.UTC).getYear();
    String invoiceNumber = numberGenerator.next(cmd.tenantId(), year);

    String sellerJson =
        cmd.sellerSnapshot() != null ? cmd.sellerSnapshot() : invoice.getSellerSnapshot();
    String buyerJson =
        cmd.buyerSnapshot() != null ? cmd.buyerSnapshot() : invoice.getBuyerSnapshot();

    // Issue the aggregate (domain invariants enforced here)
    invoice.issue(invoiceNumber, now, sellerJson, buyerJson);
    Invoice saved = invoiceRepository.save(invoice);

    // Canonicalize → hash → sign
    byte[] canonical = canonicalizer.canonicalize(saved);
    SignResult signResult = signaturePort.sign(canonical);
    String contentHash = canonicalizer.sha256Hex(canonical);

    InvoiceSignature signature =
        InvoiceSignature.create(
            saved.getId(),
            contentHash,
            signResult.algorithm(),
            signResult.signatureBase64(),
            signResult.keyId(),
            now);
    signatureRepository.save(signature);

    // Audit
    auditRepository.append(
        InvoiceAuditEntry.of(
            saved.getId(),
            cmd.tenantId(),
            Action.ISSUED,
            cmd.actorId(),
            "{\"invoiceNumber\":\""
                + invoiceNumber
                + "\",\"keyId\":\""
                + signResult.keyId()
                + "\"}",
            now));

    // Outbox (same TX — guaranteed delivery)
    outboxPort.publishIssued(cmd.tenantId(), saved.getId(), invoiceNumber, saved.getOrderId());

    log.info(
        "Invoice ISSUED: id={}, number={}, tenant={}, keyId={}",
        saved.getId(),
        invoiceNumber,
        cmd.tenantId(),
        signResult.keyId());

    return mapper.toDetailResponse(saved, signature);
  }

  /**
   * Voids an issued invoice with a mandatory reason.
   *
   * @param cmd the void command
   * @return the updated invoice response
   */
  @Transactional
  public InvoiceDetailResponse voidInvoice(VoidInvoiceCommand cmd) {
    Invoice invoice = requireByIdAndTenant(cmd.invoiceId(), cmd.tenantId());
    Instant now = clock.now();
    invoice.voidInvoice(cmd.reason(), now);
    Invoice saved = invoiceRepository.save(invoice);

    auditRepository.append(
        InvoiceAuditEntry.of(
            saved.getId(),
            cmd.tenantId(),
            Action.VOIDED,
            cmd.actorId(),
            "{\"reason\":\"" + escapeJson(cmd.reason()) + "\"}",
            now));

    outboxPort.publishVoided(cmd.tenantId(), saved.getId(), cmd.reason());

    log.info(
        "Invoice VOIDED: id={}, tenant={}, actor={}", saved.getId(), cmd.tenantId(), cmd.actorId());

    InvoiceSignature sig = signatureRepository.findByInvoiceId(saved.getId()).orElse(null);
    return mapper.toDetailResponse(saved, sig);
  }

  /**
   * Marks an invoice as paid.
   *
   * @param cmd the pay command
   * @return the updated invoice response
   */
  @Transactional
  public InvoiceDetailResponse markPaid(MarkInvoicePaidCommand cmd) {
    Invoice invoice = requireByIdAndTenant(cmd.invoiceId(), cmd.tenantId());
    Instant now = clock.now();
    invoice.markPaid(cmd.paymentReference(), now);
    Invoice saved = invoiceRepository.save(invoice);

    auditRepository.append(
        InvoiceAuditEntry.of(
            saved.getId(),
            cmd.tenantId(),
            Action.PAID,
            cmd.actorId(),
            "{\"paymentReference\":\"" + escapeJson(cmd.paymentReference()) + "\"}",
            now));

    outboxPort.publishPaid(cmd.tenantId(), saved.getId(), cmd.paymentReference());

    log.info("Invoice PAID: id={}, tenant={}, ref={}", saved.getId(), cmd.tenantId(), "[REDACTED]");

    InvoiceSignature sig = signatureRepository.findByInvoiceId(saved.getId()).orElse(null);
    return mapper.toDetailResponse(saved, sig);
  }

  // ── Queries ───────────────────────────────────────────────────────────────

  /**
   * Retrieves a single invoice detail, enforcing tenant isolation.
   *
   * @param invoiceId invoice to fetch
   * @param tenantId calling tenant
   * @return invoice detail response
   * @throws InvoiceNotFoundException if not found or belongs to another tenant (IDOR protection)
   */
  @Transactional(readOnly = true)
  public InvoiceDetailResponse getDetail(Long invoiceId, String tenantId) {
    Invoice invoice = requireByIdAndTenant(invoiceId, tenantId);
    InvoiceSignature sig = signatureRepository.findByInvoiceId(invoiceId).orElse(null);
    return mapper.toDetailResponse(invoice, sig);
  }

  /**
   * Lists invoices for a tenant using cursor/keyset pagination.
   *
   * <p>Sort: {@code (issue_date DESC, id DESC)}.
   *
   * @param tenantId calling tenant
   * @param statusFilter optional status filter
   * @param cursor opaque cursor from previous response, or null for first page
   * @param limit max items (capped at {@value #MAX_PAGE_SIZE})
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<InvoiceDetailResponse> listInvoices(
      String tenantId, InvoiceStatus statusFilter, String cursor, int limit) {
    int effectiveLimit = Math.min(Math.max(1, limit), MAX_PAGE_SIZE);
    // Fetch one extra to detect next page
    int fetchLimit = effectiveLimit + 1;

    Instant afterIssuedAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorDecoder decoded = CursorDecoder.decode(cursor);
      afterIssuedAt = decoded.issuedAt();
      afterId = decoded.id();
    }

    List<Invoice> invoices =
        invoiceRepository.findByTenantCursor(
            tenantId, statusFilter, afterIssuedAt, afterId, fetchLimit);

    boolean hasNext = invoices.size() > effectiveLimit;
    List<Invoice> page = hasNext ? invoices.subList(0, effectiveLimit) : invoices;

    List<InvoiceDetailResponse> data =
        page.stream()
            .map(
                inv -> {
                  InvoiceSignature sig =
                      signatureRepository.findByInvoiceId(inv.getId()).orElse(null);
                  return mapper.toDetailResponse(inv, sig);
                })
            .collect(Collectors.toList());

    if (!hasNext) {
      return CursorPageResponse.lastPage(data);
    }

    Invoice last = page.get(page.size() - 1);
    String nextCursor = CursorEncoder.encode(last.getIssueDate(), last.getId());
    return CursorPageResponse.withNext(data, nextCursor);
  }

  /**
   * Returns cursor-paginated audit log entries for an invoice.
   *
   * @param invoiceId invoice to query
   * @param tenantId calling tenant (for tenant scoping at query level)
   * @param cursor opaque cursor, or null for first page
   * @param limit max items (capped at {@value #MAX_PAGE_SIZE})
   */
  @Transactional(readOnly = true)
  public CursorPageResponse<AuditEntryResponse> listAuditLog(
      Long invoiceId, String tenantId, String cursor, int limit) {
    // Validate invoice belongs to tenant first (IDOR check)
    requireByIdAndTenant(invoiceId, tenantId);

    int effectiveLimit = Math.min(Math.max(1, limit), MAX_PAGE_SIZE);
    int fetchLimit = effectiveLimit + 1;

    Instant afterOccurredAt = null;
    Long afterId = null;
    if (cursor != null && !cursor.isBlank()) {
      CursorDecoder decoded = CursorDecoder.decode(cursor);
      afterOccurredAt = decoded.issuedAt();
      afterId = decoded.id();
    }

    List<InvoiceAuditEntry> entries =
        auditRepository.findByInvoiceCursor(
            invoiceId, tenantId, afterOccurredAt, afterId, fetchLimit);

    boolean hasNext = entries.size() > effectiveLimit;
    List<InvoiceAuditEntry> page = hasNext ? entries.subList(0, effectiveLimit) : entries;

    List<AuditEntryResponse> data =
        page.stream()
            .map(
                e ->
                    new AuditEntryResponse(
                        e.getId(),
                        e.getAction(),
                        e.getActorId(),
                        e.getDetails(),
                        e.getOccurredAt()))
            .collect(Collectors.toList());

    if (!hasNext) {
      return CursorPageResponse.lastPage(data);
    }

    InvoiceAuditEntry last = page.get(page.size() - 1);
    String nextCursor = CursorEncoder.encode(last.getOccurredAt(), last.getId());
    return CursorPageResponse.withNext(data, nextCursor);
  }

  /**
   * Verifies that the stored signature matches a freshly-computed canonical hash of the invoice.
   *
   * <p>Algorithm:
   *
   * <ol>
   *   <li>Fetch invoice and signature from DB.
   *   <li>Re-canonicalize the invoice to bytes.
   *   <li>Re-compute SHA-256.
   *   <li>Compare re-computed hash to stored {@code contentHash}.
   *   <li>If hash matches: verify signature bytes with the identified public key.
   * </ol>
   *
   * @param invoiceId invoice to verify
   * @param tenantId calling tenant
   * @return verification result with detailed reason on failure
   */
  @Transactional(readOnly = true)
  public SignatureVerificationResponse verifySignature(Long invoiceId, String tenantId) {
    Invoice invoice = requireByIdAndTenant(invoiceId, tenantId);

    Optional<InvoiceSignature> sigOpt = signatureRepository.findByInvoiceId(invoiceId);
    if (sigOpt.isEmpty()) {
      return new SignatureVerificationResponse(invoiceId, null, false, null, null, "NO_SIGNATURE");
    }

    InvoiceSignature sig = sigOpt.get();

    // Step 1: re-canonicalize
    byte[] canonical = canonicalizer.canonicalize(invoice);
    String recomputedHash = canonicalizer.sha256Hex(canonical);

    // Step 2: compare hash
    if (!recomputedHash.equals(sig.getContentHash())) {
      log.warn("Invoice signature hash mismatch: invoiceId={}, tenant={}", invoiceId, tenantId);
      return new SignatureVerificationResponse(
          invoiceId,
          sig.getContentHash(),
          false,
          sig.getKeyId(),
          sig.getSignedAt(),
          "HASH_MISMATCH");
    }

    // Step 3: verify signature
    boolean valid = signaturePort.verify(canonical, sig.getSignatureValue(), sig.getKeyId());

    String reason = valid ? null : "SIGNATURE_INVALID";
    return new SignatureVerificationResponse(
        invoiceId, sig.getContentHash(), valid, sig.getKeyId(), sig.getSignedAt(), reason);
  }

  /**
   * Generates a PDF for an issued invoice.
   *
   * @param invoiceId invoice to render
   * @param tenantId calling tenant
   * @return raw PDF bytes
   */
  @Transactional(readOnly = true)
  public byte[] generatePdf(Long invoiceId, String tenantId) {
    Invoice invoice = requireByIdAndTenant(invoiceId, tenantId);
    InvoiceSignature sig =
        signatureRepository
            .findByInvoiceId(invoiceId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Cannot generate PDF for invoice " + invoiceId + ": not yet signed"));
    return pdfRenderer.render(invoice, sig);
  }

  /**
   * Verifies an invoice by externally supplied signature block metadata.
   *
   * <p>This endpoint is used to authenticate a PDF invoice returned by a customer: extract the
   * signature footer values from the PDF and call this method.
   *
   * <p>Algorithm:
   *
   * <ol>
   *   <li>Lookup invoice by {@code invoiceNumber} + tenant — 404 if not found (ASVS V4.2).
   *   <li>Load stored {@link InvoiceSignature}.
   *   <li>Compare provided {@code contentHash} with stored — reason: {@code HASH_MISMATCH}.
   *   <li>Verify provided signature bytes against the stored hash using the public key for {@code
   *       keyId}.
   *   <li>Return result: {@code signatureValid=true} only when hash matches AND signature is valid.
   * </ol>
   *
   * @param invoiceNumber human-readable invoice number from the PDF footer
   * @param contentHash SHA-256 hex hash as printed in the PDF
   * @param signatureValue Ed25519 signature (Base64) as printed in the PDF
   * @param keyId key rotation ID as printed in the PDF
   * @param tenantId calling tenant (from JWT — ASVS V4.1)
   */
  @Transactional(readOnly = true)
  public SignatureVerificationResponse verifyBySignatureBlock(
      String invoiceNumber,
      String contentHash,
      String signatureValue,
      String keyId,
      String tenantId) {

    Invoice invoice =
        invoiceRepository
            .findByInvoiceNumberAndTenant(invoiceNumber, tenantId)
            .orElseThrow(
                () ->
                    new InvoiceNotFoundException(
                        "Invoice not found for invoiceNumber='"
                            + invoiceNumber
                            + "' tenant='"
                            + tenantId
                            + "'"));

    Optional<InvoiceSignature> sigOpt = signatureRepository.findByInvoiceId(invoice.getId());
    if (sigOpt.isEmpty()) {
      log.warn(
          "[{}] invoiceNumber={} tenant={} — no signature record",
          InvoiceLogEvents.INVOICE_VERIFICATION_FAILED,
          invoiceNumber,
          tenantId);
      return new SignatureVerificationResponse(
          invoice.getId(), null, false, null, null, "NO_SIGNATURE");
    }

    InvoiceSignature sig = sigOpt.get();

    // Step 1: compare provided hash against stored hash
    if (!contentHash.equalsIgnoreCase(sig.getContentHash())) {
      log.warn(
          "[{}] invoiceNumber={} tenant={} — contentHash mismatch",
          InvoiceLogEvents.INVOICE_VERIFICATION_FAILED,
          invoiceNumber,
          tenantId);
      return new SignatureVerificationResponse(
          invoice.getId(),
          sig.getContentHash(),
          false,
          sig.getKeyId(),
          sig.getSignedAt(),
          "HASH_MISMATCH");
    }

    // Step 2: verify the provided signature value using the public key identified by keyId
    byte[] hashBytes = contentHash.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    boolean valid = signaturePort.verify(hashBytes, signatureValue, keyId);

    String reason = valid ? null : "INVALID_SIGNATURE";
    if (!valid) {
      log.warn(
          "[{}] invoiceNumber={} tenant={} keyId={} — signature invalid",
          InvoiceLogEvents.INVOICE_VERIFICATION_FAILED,
          invoiceNumber,
          tenantId,
          keyId);
    } else {
      log.info(
          "[{}] invoiceNumber={} tenant={} keyId={}",
          InvoiceLogEvents.INVOICE_VERIFIED,
          invoiceNumber,
          tenantId,
          keyId);
    }

    return new SignatureVerificationResponse(
        invoice.getId(), sig.getContentHash(), valid, keyId, sig.getSignedAt(), reason);
  }

  /**
   * Re-queues an invoice email delivery for the given invoice.
   *
   * <p>Creates a new {@link InvoiceEmailDelivery} record with type {@code INVOICE_RESEND} (separate
   * idempotency key from the original INVOICE_ISSUED send). The outbox handler will pick it up on
   * the next relay poll.
   *
   * @param invoiceId invoice to resend
   * @param tenantId calling tenant
   */
  @Transactional
  public void resendEmail(Long invoiceId, String tenantId) {
    Invoice invoice = requireByIdAndTenant(invoiceId, tenantId);

    String idempotencyKey = invoiceId + ":" + MessageType.INVOICE_RESEND.name();
    Instant now = clock.now();

    // Upsert: if a RESEND record already exists and is PENDING/FAILED, reset it; else create new
    InvoiceEmailDelivery delivery =
        emailDeliveryPort
            .findByIdempotencyKey(idempotencyKey)
            .orElseGet(
                () -> {
                  // Derive recipient from buyer snapshot (same logic as handler)
                  String buyerEmail = extractEmailFromSnapshot(invoice.getBuyerSnapshot());
                  return InvoiceEmailDelivery.pending(
                      invoiceId,
                      tenantId,
                      buyerEmail != null ? buyerEmail : "",
                      MessageType.INVOICE_RESEND,
                      now);
                });

    // Reset to PENDING to allow re-sending
    if (delivery.getStatus() != Status.PENDING) {
      delivery.setStatus(Status.PENDING);
      delivery.setLastAttemptedAt(null);
      delivery.setErrorMessage(null);
    }
    emailDeliveryPort.save(delivery);

    auditRepository.append(
        InvoiceAuditEntry.of(
            invoiceId,
            tenantId,
            Action.EMAIL_SENT,
            tenantId,
            "{\"action\":\"resend_queued\"}",
            now));

    log.info(
        "[{}] invoiceId={} tenant={} — resend email queued",
        InvoiceLogEvents.INVOICE_EMAIL_SENT,
        invoiceId,
        tenantId);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private Invoice requireByIdAndTenant(Long id, String tenantId) {
    return invoiceRepository
        .findByIdAndTenant(id, tenantId)
        .orElseThrow(() -> new InvoiceNotFoundException(id, tenantId));
  }

  private static String extractEmailFromSnapshot(String snapshot) {
    if (snapshot == null) return null;
    int idx = snapshot.indexOf("\"email\"");
    if (idx < 0) return null;
    int colon = snapshot.indexOf(':', idx);
    if (colon < 0) return null;
    int start = snapshot.indexOf('"', colon + 1);
    if (start < 0) return null;
    int end = snapshot.indexOf('"', start + 1);
    if (end < 0) return null;
    return snapshot.substring(start + 1, end);
  }

  /** Minimal JSON string escaping — avoids pulling in Jackson at this level. */
  private static String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\\\", "\\\\\\\\").replace("\"", "\\\"");
  }
}
