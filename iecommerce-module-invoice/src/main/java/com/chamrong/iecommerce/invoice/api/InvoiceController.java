package com.chamrong.iecommerce.invoice.api;

import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.invoice.application.InvoiceApplicationService;
import com.chamrong.iecommerce.invoice.application.command.CreateInvoiceDraftCommand;
import com.chamrong.iecommerce.invoice.application.command.IssueInvoiceCommand;
import com.chamrong.iecommerce.invoice.application.command.MarkInvoicePaidCommand;
import com.chamrong.iecommerce.invoice.application.command.VoidInvoiceCommand;
import com.chamrong.iecommerce.invoice.application.dto.AuditEntryResponse;
import com.chamrong.iecommerce.invoice.application.dto.CursorPageResponse;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceDetailResponse;
import com.chamrong.iecommerce.invoice.application.dto.SignatureVerificationResponse;
import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the Invoice bounded context.
 *
 * <h2>Security</h2>
 *
 * <ul>
 *   <li>ASVS V4.1 — Tenant ID is always extracted from the JWT, never from user-supplied params.
 *   <li>ASVS V4.2 — All findById calls are tenant-scoped; 404 returned (not 403) to prevent IDOR.
 *   <li>ASVS V5.1 — All inputs validated via Bean Validation.
 *   <li>ASVS V7.1 — No stack traces or internal details returned on error.
 * </ul>
 *
 * <p>Base path: {@code /api/v1/invoices}
 */
@Tag(
    name = "Invoices",
    description = "Invoice lifecycle management — creation, issuance, voiding, payment, signing")
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAuthority('"
        + Permissions.INVOICE_READ
        + "') or hasAuthority('"
        + Permissions.INVOICE_MANAGE
        + "')")
public class InvoiceController {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final InvoiceApplicationService invoiceService;

  // ── POST /api/v1/invoices ──────────────────────────────────────────────────

  @Operation(
      summary = "Create a DRAFT invoice",
      description =
          "Creates a new invoice in DRAFT status. Line items may be added before issuance.",
      responses = {
        @ApiResponse(responseCode = "201", description = "Draft created"),
        @ApiResponse(responseCode = "400", description = "Validation failure")
      })
  @PostMapping
  @PreAuthorize("hasAuthority('" + Permissions.INVOICE_MANAGE + "')")
  public ResponseEntity<InvoiceDetailResponse> createDraft(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateDraftRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    String actorId = jwt.getSubject();

    List<CreateInvoiceDraftCommand.LineItem> lines =
        req.lines().stream()
            .map(
                l ->
                    new CreateInvoiceDraftCommand.LineItem(
                        l.sku(),
                        l.productName(),
                        l.description(),
                        l.quantity(),
                        l.unitPriceAmount(),
                        l.taxRate(),
                        l.lineOrder()))
            .toList();

    CreateInvoiceDraftCommand cmd =
        new CreateInvoiceDraftCommand(
            tenantId,
            actorId,
            req.orderId(),
            req.customerId(),
            req.currency(),
            req.dueDate(),
            req.sellerSnapshot(),
            req.buyerSnapshot(),
            lines);

    InvoiceDetailResponse response = invoiceService.createDraft(cmd);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // ── GET /api/v1/invoices ───────────────────────────────────────────────────

  @Operation(
      summary = "List invoices (cursor paginated)",
      description = "Returns invoices for the calling tenant, sorted by issue_date DESC, id DESC.")
  @GetMapping
  public ResponseEntity<CursorPageResponse<InvoiceDetailResponse>> listInvoices(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) InvoiceStatus status,
      @RequestParam(required = false) String cursor,
      @Parameter(description = "Page size, 1-100", schema = @Schema(defaultValue = "20"))
          @RequestParam(defaultValue = "20")
          @Min(1)
          @Max(MAX_PAGE_SIZE)
          int limit) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    return ResponseEntity.ok(invoiceService.listInvoices(tenantId, status, cursor, limit));
  }

  // ── GET /api/v1/invoices/{id} ──────────────────────────────────────────────

  @Operation(
      summary = "Get invoice detail",
      responses = {
        @ApiResponse(responseCode = "200", description = "Invoice found"),
        @ApiResponse(responseCode = "404", description = "Not found or cross-tenant access attempt")
      })
  @GetMapping("/{id}")
  public ResponseEntity<InvoiceDetailResponse> getById(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    return ResponseEntity.ok(invoiceService.getDetail(id, tenantId));
  }

  // ── POST /api/v1/invoices/{id}/issue ──────────────────────────────────────

  @Operation(
      summary = "Issue invoice",
      description = "DRAFT → ISSUED. Assigns invoice number, signs with Ed25519, locks content.")
  @PostMapping("/{id}/issue")
  @PreAuthorize("hasAuthority('" + Permissions.INVOICE_MANAGE + "')")
  public ResponseEntity<InvoiceDetailResponse> issue(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @RequestBody(required = false) IssueRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    String actorId = jwt.getSubject();
    IssueInvoiceCommand cmd =
        new IssueInvoiceCommand(
            tenantId,
            actorId,
            id,
            req != null ? req.sellerSnapshot() : null,
            req != null ? req.buyerSnapshot() : null);
    return ResponseEntity.ok(invoiceService.issueInvoice(cmd));
  }

  // ── POST /api/v1/invoices/{id}/void ───────────────────────────────────────

  @Operation(
      summary = "Void invoice",
      description = "ISSUED/OVERDUE → VOIDED. Reason is mandatory.")
  @PostMapping("/{id}/void")
  @PreAuthorize("hasAuthority('" + Permissions.INVOICE_MANAGE + "')")
  public ResponseEntity<InvoiceDetailResponse> voidInvoice(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @Valid @RequestBody VoidRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    String actorId = jwt.getSubject();
    return ResponseEntity.ok(
        invoiceService.voidInvoice(new VoidInvoiceCommand(tenantId, actorId, id, req.reason())));
  }

  // ── POST /api/v1/invoices/{id}/pay ────────────────────────────────────────

  @Operation(summary = "Mark invoice paid", description = "ISSUED/OVERDUE → PAID.")
  @PostMapping("/{id}/pay")
  @PreAuthorize("hasAuthority('" + Permissions.INVOICE_MANAGE + "')")
  public ResponseEntity<InvoiceDetailResponse> markPaid(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody PayRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    String actorId = jwt.getSubject();
    return ResponseEntity.ok(
        invoiceService.markPaid(
            new MarkInvoicePaidCommand(tenantId, actorId, id, req.paymentReference())));
  }

  // ── GET /api/v1/invoices/{id}/verify ──────────────────────────────────────

  @Operation(
      summary = "Verify digital signature",
      description =
          "Re-canonicalizes the invoice, recomputes SHA-256 hash, and verifies Ed25519 signature.")
  @GetMapping("/{id}/verify")
  public ResponseEntity<SignatureVerificationResponse> verifySignature(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    return ResponseEntity.ok(invoiceService.verifySignature(id, tenantId));
  }

  // ── GET /api/v1/invoices/{id}/pdf ─────────────────────────────────────────

  @Operation(
      summary = "Download invoice PDF",
      description =
          "Generates a PDF with line items, totals, signature footer, and verification QR code.")
  @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> downloadPdf(
      @AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    byte[] pdf = invoiceService.generatePdf(id, tenantId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
    return ResponseEntity.ok().headers(headers).body(pdf);
  }

  // ── GET /api/v1/invoices/{id}/audit-log ───────────────────────────────────

  @Operation(summary = "Get invoice audit log", description = "Cursor-paginated audit trail.")
  @GetMapping("/{id}/audit-log")
  public ResponseEntity<CursorPageResponse<AuditEntryResponse>> getAuditLog(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long id,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int limit) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    return ResponseEntity.ok(invoiceService.listAuditLog(id, tenantId, cursor, limit));
  }

  // ── POST /api/v1/invoices/verify ──────────────────────────────────────────

  /**
   * Verifies an invoice by externally supplied signature block metadata extracted from a PDF.
   *
   * <p>Use case: a third party received a signed PDF invoice and wants to confirm it is authentic
   * and has not been tampered with. They extract the signature footer values from the PDF and call
   * this endpoint.
   *
   * <p>ASVS V4.1 — Tenant ID is extracted from the JWT, never from request body. ASVS V4.2 — 404
   * returned when invoice belongs to a different tenant (no IDOR).
   */
  @Operation(
      summary = "Verify invoice by signature block",
      description =
          """
Verifies an invoice using signature metadata extracted from a PDF footer.
Returns signatureValid=true only when the contentHash matches AND the Ed25519 signature is valid.
Reason codes on failure: HASH_MISMATCH, INVALID_SIGNATURE, NO_SIGNATURE.
""")
  @ApiResponse(
      responseCode = "200",
      description = "Verification result (may be signatureValid=false with a reason)")
  @ApiResponse(responseCode = "404", description = "Invoice not found for this tenant")
  @PostMapping("/verify")
  @PreAuthorize(
      "hasAuthority('"
          + Permissions.INVOICE_READ
          + "') or hasAuthority('"
          + Permissions.INVOICE_MANAGE
          + "')")
  public ResponseEntity<SignatureVerificationResponse> verifyBySignatureBlock(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody VerifyBySignatureBlockRequest req) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    return ResponseEntity.ok(
        invoiceService.verifyBySignatureBlock(
            req.invoiceNumber(), req.contentHash(), req.signatureValue(), req.keyId(), tenantId));
  }

  // ── POST /api/v1/invoices/{id}/resend-email ────────────────────────────────

  @Operation(
      summary = "Re-queue invoice email delivery",
      description =
          "Re-queues a signed invoice email for delivery. Idempotent — creates a new INVOICE_RESEND"
              + " delivery record.")
  @PostMapping("/{id}/resend-email")
  @PreAuthorize("hasAuthority('" + Permissions.INVOICE_MANAGE + "')")
  public ResponseEntity<Void> resendEmail(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    String tenantId = jwt.getClaimAsString("tenant_id");
    invoiceService.resendEmail(id, tenantId);
    return ResponseEntity.accepted().build();
  }

  // ── Request DTOs ───────────────────────────────────────────────────────────

  /**
   * Request body for creating a draft invoice.
   *
   * <p>ASVS V5.1 — All fields validated.
   */
  public record CreateDraftRequest(
      Long orderId,
      Long customerId,
      @NotBlank @Size(min = 3, max = 3) String currency,
      @NotNull LocalDate dueDate,
      @Size(max = 4000) String sellerSnapshot,
      @Size(max = 4000) String buyerSnapshot,
      @NotNull List<@Valid LineItemRequest> lines) {

    public record LineItemRequest(
        @Size(max = 100) String sku,
        @NotBlank @Size(max = 255) String productName,
        @Size(max = 1000) String description,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPriceAmount,
        @NotNull BigDecimal taxRate,
        @Min(0) int lineOrder) {}
  }

  /** Optional request for updating snapshots during issue. */
  public record IssueRequest(
      @Size(max = 4000) String sellerSnapshot, @Size(max = 4000) String buyerSnapshot) {}

  /** Request for voiding — reason is mandatory. */
  public record VoidRequest(@NotBlank @Size(min = 5, max = 500) String reason) {}

  /** Request for marking paid. */
  public record PayRequest(@NotBlank @Size(max = 255) String paymentReference) {}

  /**
   * Request for POST /invoices/verify — signature block extracted from a signed PDF footer.
   *
   * <p>ASVS V5.1 — Fields validated; size limits guard against oversized inputs. ASVS V11.1.4 —
   * Rate limiting applied at infrastructure level (Resilience4j).
   */
  public record VerifyBySignatureBlockRequest(
      @NotBlank @Size(max = 100) String invoiceNumber,
      @NotBlank @Size(min = 64, max = 128) String contentHash,
      @NotBlank @Size(max = 256) String signatureValue,
      @NotBlank @Size(max = 100) String keyId,
      @Size(max = 20) String signatureAlgorithm) {}
}
