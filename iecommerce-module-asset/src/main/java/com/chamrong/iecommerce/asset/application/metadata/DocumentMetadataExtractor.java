package com.chamrong.iecommerce.asset.application.metadata;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Component;

/** Extracts metadata from document files. Currently focuses on PDF using Apache PDFBox. */
@Slf4j
@Component
public class DocumentMetadataExtractor implements MetadataExtractor {

  @Override
  public boolean supports(String mimeType) {
    return "application/pdf".equals(mimeType);
  }

  @Override
  public Map<String, Object> extract(InputStream inputStream, String mimeType) {
    Map<String, Object> metadata = new HashMap<>();
    if (!"application/pdf".equals(mimeType)) {
      return metadata;
    }

    try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
      metadata.put("pages", document.getNumberOfPages());

      PDDocumentInformation info = document.getDocumentInformation();
      if (info != null) {
        if (info.getTitle() != null) metadata.put("title", info.getTitle());
        if (info.getAuthor() != null) metadata.put("author", info.getAuthor());
        if (info.getSubject() != null) metadata.put("subject", info.getSubject());
        if (info.getCreator() != null) metadata.put("creator", info.getCreator());
      }
    } catch (Exception e) {
      log.warn("Failed to extract PDF metadata: {}", e.getMessage());
    }
    return metadata;
  }
}
