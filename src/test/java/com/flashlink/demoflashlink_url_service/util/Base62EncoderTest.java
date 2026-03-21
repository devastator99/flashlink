package com.flashlink.demoflashlink_url_service.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Base62Encoder Tests")
class Base62EncoderTest {

    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    @DisplayName("Should encode zero correctly")
    void encodeZero_ShouldReturnValidString() {
        // Given
        long id = 0L;

        // When
        String result = encoder.encode(id);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    @DisplayName("Should encode positive numbers correctly")
    void encodePositiveNumber_ShouldReturnValidString() {
        // Given
        long id = 12345L;

        // When
        String result = encoder.encode(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).matches("^[a-zA-Z0-9]+$");
    }

    @Test
    @DisplayName("Should encode large numbers correctly")
    void encodeLargeNumber_ShouldReturnValidString() {
        // Given
        long id = Long.MAX_VALUE;

        // When
        String result = encoder.encode(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).matches("^[a-zA-Z0-9]+$");
    }

    @Test
    @DisplayName("Should produce consistent results for same input")
    void encodeSameInput_ShouldProduceConsistentResults() {
        // Given
        long id = 123456789L;

        // When
        String result1 = encoder.encode(id);
        String result2 = encoder.encode(id);

        // Then
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("Should produce different results for different inputs")
    void encodeDifferentInputs_ShouldProduceDifferentResults() {
        // Given
        long id1 = 12345L;
        long id2 = 54321L;

        // When
        String result1 = encoder.encode(id1);
        String result2 = encoder.encode(id2);

        // Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("Should produce URL-safe strings")
    void encode_ShouldProduceUrlSafeStrings() {
        // Given
        long[] testIds = {0L, 1L, 123L, 999999L, Long.MAX_VALUE};

        // When & Then
        for (long id : testIds) {
            String result = encoder.encode(id);
            assertThat(result).matches("^[a-zA-Z0-9]+$");
            // Should not contain URL-unsafe characters
            assertThat(result).doesNotContain("+");
            assertThat(result).doesNotContain("/");
            assertThat(result).doesNotContain("=");
        }
    }

    @Test
    @DisplayName("Should handle edge cases")
    void encodeEdgeCases_ShouldHandleCorrectly() {
        // Given & When & Then
        assertThat(encoder.encode(1L)).isNotEqualTo(encoder.encode(0L));
        assertThat(encoder.encode(Long.MIN_VALUE)).isNotNull();
        assertThat(encoder.encode(Long.MAX_VALUE)).isNotNull();
    }

    @Test
    @DisplayName("Should produce reasonably short strings")
    void encode_ShouldProduceReasonableLengthStrings() {
        // Given
        long id = 123456789012345L; // Large number

        // When
        String result = encoder.encode(id);

        // Then
        assertThat(result.length()).isLessThan(20); // Should be reasonably short
    }
}
