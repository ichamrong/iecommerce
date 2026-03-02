package com.chamrong.iecommerce.report.domain.model;

/** Lifecycle status of an export job. */
public enum ExportStatus {
  PENDING,
  RUNNING,
  COMPLETED,
  FAILED,
  CANCELLED
}
