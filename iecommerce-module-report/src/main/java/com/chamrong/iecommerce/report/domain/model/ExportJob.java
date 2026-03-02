package com.chamrong.iecommerce.report.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate representing a long-running export job.
 *
 * <p>Pure domain model; persistence mapping lives in infrastructure.
 */
public final class ExportJob {

  private final UUID id;
  private final String tenantId;
  private final ReportType reportType;
  private final String format;
  private final String filtersJson;
  private final String filtersHash;
  private final Instant createdAt;
  private final String createdBy;
  private final IdempotencyKey idempotencyKey;

  private ExportStatus status;
  private String fileRef;
  private Long rowCount;
  private String contentSha256;
  private String signature;
  private String signatureAlg;
  private String keyId;
  private Instant completedAt;
  private String errorCode;

  public ExportJob(
      UUID id,
      String tenantId,
      ReportType reportType,
      String format,
      String filtersJson,
      String filtersHash,
      Instant createdAt,
      String createdBy,
      IdempotencyKey idempotencyKey,
      ExportStatus status) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
    this.reportType = Objects.requireNonNull(reportType, "reportType must not be null");
    this.format = Objects.requireNonNull(format, "format must not be null");
    this.filtersJson = filtersJson;
    this.filtersHash = filtersHash;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    this.createdBy = createdBy;
    this.idempotencyKey = idempotencyKey;
    this.status = Objects.requireNonNull(status, "status must not be null");
  }

  public static ExportJob pending(
      String tenantId,
      ReportType reportType,
      String format,
      String filtersJson,
      String filtersHash,
      Instant createdAt,
      String createdBy,
      IdempotencyKey idempotencyKey) {
    return new ExportJob(
        UUID.randomUUID(),
        tenantId,
        reportType,
        format,
        filtersJson,
        filtersHash,
        createdAt,
        createdBy,
        idempotencyKey,
        ExportStatus.PENDING);
  }

  public UUID getId() {
    return id;
  }

  public String getTenantId() {
    return tenantId;
  }

  public ReportType getReportType() {
    return reportType;
  }

  public String getFormat() {
    return format;
  }

  public String getFiltersJson() {
    return filtersJson;
  }

  public String getFiltersHash() {
    return filtersHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public IdempotencyKey getIdempotencyKey() {
    return idempotencyKey;
  }

  public ExportStatus getStatus() {
    return status;
  }

  public String getFileRef() {
    return fileRef;
  }

  public Long getRowCount() {
    return rowCount;
  }

  public String getContentSha256() {
    return contentSha256;
  }

  public String getSignature() {
    return signature;
  }

  public String getSignatureAlg() {
    return signatureAlg;
  }

  public String getKeyId() {
    return keyId;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void markRunning() {
    if (status == ExportStatus.CANCELLED) {
      throw new IllegalStateException("Cannot start cancelled export job");
    }
    this.status = ExportStatus.RUNNING;
  }

  public void markCompleted(
      String fileRef,
      long rowCount,
      String contentSha256,
      String signature,
      String signatureAlg,
      String keyId,
      Instant completedAt) {
    this.status = ExportStatus.COMPLETED;
    this.fileRef = fileRef;
    this.rowCount = rowCount;
    this.contentSha256 = contentSha256;
    this.signature = signature;
    this.signatureAlg = signatureAlg;
    this.keyId = keyId;
    this.completedAt = completedAt;
  }

  public void markFailed(String errorCode, Instant completedAt) {
    this.status = ExportStatus.FAILED;
    this.errorCode = errorCode;
    this.completedAt = completedAt;
  }

  public void cancel() {
    if (status == ExportStatus.COMPLETED) {
      throw new IllegalStateException("Cannot cancel completed export job");
    }
    this.status = ExportStatus.CANCELLED;
  }
}
