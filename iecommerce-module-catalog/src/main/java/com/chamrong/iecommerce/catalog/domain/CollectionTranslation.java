package com.chamrong.iecommerce.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

/** Locale-specific display name and description for a {@link Collection}. */
@Entity
@Table(
    name = "catalog_collection_translations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_collection_translation_locale",
            columnNames = {"collection_id", "locale"}))
public class CollectionTranslation {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "collection_id", nullable = false, updatable = false)
  private Collection collection;

  @Getter
  @Column(nullable = false, length = 10, updatable = false)
  private String locale;

  @Getter
  @Column(nullable = false, length = 255)
  private String name;

  @Getter
  @Column(columnDefinition = "TEXT")
  private String description;

  protected CollectionTranslation() {}

  public CollectionTranslation(
      Collection collection, String locale, String name, String description) {
    this.collection = collection;
    this.locale = locale;
    this.name = name;
    this.description = description;
  }

  public void update(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
