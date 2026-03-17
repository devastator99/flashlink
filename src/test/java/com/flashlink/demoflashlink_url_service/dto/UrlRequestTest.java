package com.flashlink.demoflashlink_url_service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UrlRequest Validation Tests")
class UrlRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate valid HTTP URL")
    void validHttpUrl_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("http://example.com");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate valid HTTPS URL")
    void validHttpsUrl_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://example.com");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate URL with path")
    void validUrlWithPath_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://example.com/path/to/resource");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate URL with query parameters")
    void validUrlWithQueryParams_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://example.com/search?q=test&page=1");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate URL with port")
    void validUrlWithPort_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://example.com:8080/path");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject null URL")
    void nullUrl_ShouldFailValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl(null);

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // @NotNull and @URL violations
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("must not be null");
    }

    @Test
    @DisplayName("Should reject empty URL")
    void emptyUrl_ShouldFailValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid URL");
    }

    @Test
    @DisplayName("Should reject blank URL")
    void blankUrl_ShouldFailValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("   ");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid URL");
    }

    @Test
    @DisplayName("Should reject FTP URL")
    void ftpUrl_ShouldFailValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("ftp://example.com");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid URL");
    }

    @Test
    @DisplayName("Should reject URL without protocol")
    void urlWithoutProtocol_ShouldFailValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("example.com");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid URL");
    }

    @Test
    @DisplayName("Should reject malformed URL")
    void malformedUrl_ShouldFailValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("http://");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid URL");
    }

    @Test
    @DisplayName("Should validate localhost URL")
    void localhostUrl_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("http://localhost:8080");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate IP address URL")
    void ipAddressUrl_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://192.168.1.1");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate URL with fragment")
    void urlWithFragment_ShouldPassValidation() {
        // Given
        UrlRequest request = new UrlRequest();
        request.setLongUrl("https://example.com/page#section");

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject extremely long URL")
    void extremelyLongUrl_ShouldFailValidation() {
        // Given
        String longUrl = "https://example.com/" + "a".repeat(10000);
        UrlRequest request = new UrlRequest();
        request.setLongUrl(longUrl);

        // When
        Set<ConstraintViolation<UrlRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid URL");
    }
}
