package com.chamrong.iecommerce.catalog.domain.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CatalogValidationPolicyTest {

  @Nested
  @DisplayName("validateSlug")
  class ValidateSlug {

    @Test
    void acceptsLowercaseAlphanumericAndHyphens() {
      assertThatCode(() -> CatalogValidationPolicy.validateSlug("valid-slug")).doesNotThrowAnyException();
      assertThatCode(() -> CatalogValidationPolicy.validateSlug("a")).doesNotThrowAnyException();
      assertThatCode(() -> CatalogValidationPolicy.validateSlug("abc123")).doesNotThrowAnyException();
    }

    @Test
    void rejectsNull() {
      assertThatThrownBy(() -> CatalogValidationPolicy.validateSlug(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("required");
    }

    @Test
    void rejectsBlank() {
      assertThatThrownBy(() -> CatalogValidationPolicy.validateSlug("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("required");
    }

    @Test
    void rejectsInvalidFormat() {
      assertThatThrownBy(() -> CatalogValidationPolicy.validateSlug("Uppercase"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("lowercase");
      assertThatThrownBy(() -> CatalogValidationPolicy.validateSlug("has space"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("validateSku")
  class ValidateSku {

    @Test
    void acceptsNullOrNonBlank() {
      assertThatCode(() -> CatalogValidationPolicy.validateSku(null)).doesNotThrowAnyException();
      assertThatCode(() -> CatalogValidationPolicy.validateSku("SKU-001")).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankWhenProvided() {
      assertThatThrownBy(() -> CatalogValidationPolicy.validateSku("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("blank");
    }
  }
}
