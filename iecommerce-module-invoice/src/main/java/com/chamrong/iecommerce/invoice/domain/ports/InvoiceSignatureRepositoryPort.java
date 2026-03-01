package com.chamrong.iecommerce.invoice.domain.ports;

import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import java.util.Optional;

/**
 * Output port: persistence for {@link InvoiceSignature} records.
 *
 * <p>One signature per invoice — enforced by the unique constraint {@code uk_invoice_signature}.
 */
public interface InvoiceSignatureRepositoryPort {

  /**
   * Persists a new signature record. Throws if a signature already exists for the same invoice.
   *
   * @param signature the signature to persist
   * @return the saved entity
   */
  InvoiceSignature save(InvoiceSignature signature);

  /**
   * Retrieves the signature for a given invoice.
   *
   * @param invoiceId invoice primary key
   */
  Optional<InvoiceSignature> findByInvoiceId(Long invoiceId);
}
